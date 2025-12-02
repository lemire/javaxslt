import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.swing.plaf.FontUIResource;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.awt.RenderingHints;
import java.awt.Taskbar;

public class XsltTransformerApp extends JFrame {

    private RSyntaxTextArea xsltArea;
    private RSyntaxTextArea xmlArea;
    private RSyntaxTextArea resultArea;
    private JButton transformButton;
    private boolean xmlValid = true;
    private boolean xsltValid = true;
    private JLabel xsltErrorLabel;
    private JLabel xmlErrorLabel;
    private JLabel statusLabel;

    public XsltTransformerApp() {
        setTitle("Transformateur XSLT");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configure a modern look and feel and fonts
        setupLookAndFeel();
        
        // Generate and set a custom application icon (window + taskbar/dock when supported)
        try {
            java.awt.Image appImg = createAppIconImage(64, 64);
            setIconImage(appImg);
            try {
                Taskbar tb = Taskbar.getTaskbar();
                tb.setIconImage(appImg);
            } catch (UnsupportedOperationException | SecurityException e) {
                // Taskbar not supported on this platform or no permission; ignore
            }
        } catch (Exception ignored) {}

        initComponents();
    }

    private void setupLookAndFeel() {
        try {
            // Prefer Nimbus if available for a modern appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // ignore and fall back to default
        }

        // Set a modern default font for UI elements
        FontUIResource uiFont = new FontUIResource("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextArea.font", new FontUIResource("Monospaced", Font.PLAIN, 13));
        UIManager.put("TextField.font", uiFont);
        UIManager.put("Menu.font", uiFont);
        UIManager.put("ToggleButton.font", uiFont);
        UIManager.put("TabbedPane.font", uiFont);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with two editors side-by-side
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplit.setResizeWeight(0.5);

        xsltErrorLabel = new JLabel();
        xsltErrorLabel.setForeground(Color.RED);
        JPanel xsltPanel = createEditorPanel("XSLT", SyntaxConstants.SYNTAX_STYLE_XML, getSampleXslt(), xsltErrorLabel);
        xsltArea = getTextAreaFromPanel(xsltPanel);
        topSplit.setLeftComponent(xsltPanel);

        xmlErrorLabel = new JLabel();
        xmlErrorLabel.setForeground(Color.RED);
        JPanel xmlPanel = createEditorPanel("XML d'entrée", SyntaxConstants.SYNTAX_STYLE_XML, getSampleXml(), xmlErrorLabel);
        xmlArea = getTextAreaFromPanel(xmlPanel);
        topSplit.setRightComponent(xmlPanel);

        // Bottom result area
        JPanel resultPanel = createEditorPanel("Résultat de la transformation", SyntaxConstants.SYNTAX_STYLE_XML, "", null);
        resultArea = getTextAreaFromPanel(resultPanel);
        resultArea.setEditable(false);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, resultPanel);
        mainSplit.setResizeWeight(0.5);

        mainPanel.add(mainSplit, BorderLayout.CENTER);

        // Button panel
        transformButton = new JButton("Appliquer le XSLT →");
        transformButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performTransformation();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(transformButton);

        // Status bar below the button
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Attach document listeners for live validation
        xsltArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateXslt(); }
            @Override public void removeUpdate(DocumentEvent e) { validateXslt(); }
            @Override public void changedUpdate(DocumentEvent e) { validateXslt(); }
        });

        xmlArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateXml(); }
            @Override public void removeUpdate(DocumentEvent e) { validateXml(); }
            @Override public void changedUpdate(DocumentEvent e) { validateXml(); }
        });

        // Initial validation
        validateXslt();
        validateXml();
    }

    private void validateXslt() {
        String text = xsltArea.getText();
        if (text.trim().isEmpty()) {
            xsltValid = false;
            xsltErrorLabel.setText("Le XSLT est vide");
            statusLabel.setText("Le XSLT est vide");
            updateButton();
            return;
        }

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            // try to compile templates to validate XSLT
            factory.newTemplates(new StreamSource(new StringReader(text)));
            xsltValid = true;
            xsltErrorLabel.setText("");
            // if XML has error it will be shown by validateXml(); prefer showing Prêt only when both valid
            if (xmlValid) statusLabel.setText("Prêt");
        } catch (TransformerConfigurationException tce) {
            xsltValid = false;
            String msg = tce.getMessage();
            xsltErrorLabel.setText("XSLT invalide");
            statusLabel.setText("Erreur XSLT: " + (msg != null ? msg.split("\n")[0] : tce.toString()));
        } catch (Exception e) {
            xsltValid = false;
            xsltErrorLabel.setText("XSLT invalide");
            statusLabel.setText("Erreur XSLT: " + e.getMessage());
        }
        updateButton();
    }

    private void validateXml() {
        String text = xmlArea.getText();
        if (text.trim().isEmpty()) {
            xmlValid = false;
            xmlErrorLabel.setText("Le XML est vide");
            statusLabel.setText("Le XML est vide");
            updateButton();
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            // parse to check well-formedness
            builder.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
            xmlValid = true;
            xmlErrorLabel.setText("");
            if (xsltValid) statusLabel.setText("Prêt");
        } catch (Exception e) {
            xmlValid = false;
            String msg = e.getMessage();
            xmlErrorLabel.setText("XML invalide");
            statusLabel.setText("Erreur XML: " + (msg != null ? msg.split("\n")[0] : e.toString()));
        }
        updateButton();
    }

    private void updateButton() {
        transformButton.setEnabled(xmlValid && xsltValid);
    }

    private boolean isValidXml(String text) {
        if (text.trim().isEmpty()) return false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private JPanel createEditorPanel(String title, String syntax, String initialText, JLabel errorLabel) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 40);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setSyntaxEditingStyle(syntax);
        textArea.setText(initialText);
        textArea.setCaretPosition(0);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        // scrollPane.getGutter().setLineNumberingColor(new Color(100, 100, 100));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, BorderLayout.NORTH);

        if (errorLabel != null) {
            panel.add(errorLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    private void performTransformation() {
        String xsltText = xsltArea.getText();
        String xmlText = xmlArea.getText();

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(new StringReader(xsltText)));

            StringWriter writer = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(xmlText)), new StreamResult(writer));

            resultArea.setText(writer.toString());
            resultArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            resultArea.setCaretPosition(0);
        } catch (Exception ex) {
            String error = "Transformation failed:\n\n" + ex.getMessage();
            if (ex instanceof TransformerException) {
                TransformerException te = (TransformerException) ex;
                String loc = te.getLocationAsString();
                if (loc != null) {
                    error += "\n" + loc;
                }
            }
            resultArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            resultArea.setText(error);
            ex.printStackTrace();
        }
    }

    // Utility: find the RSyntaxTextArea inside the custom editor panel
    private RSyntaxTextArea getTextAreaFromPanel(JPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof RTextScrollPane) {
                RTextScrollPane rsp = (RTextScrollPane) c;
                Component view = rsp.getViewport().getView();
                if (view instanceof RSyntaxTextArea) {
                    return (RSyntaxTextArea) view;
                }
            }
        }
        return null;
    }

    // Create a simple programmatic app icon (rounded square with a stylized "<X>" glyph)
    private Image createAppIconImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background gradient
            Color c1 = new Color(0x2D9CDB); // blue
            Color c2 = new Color(0x2BB673); // green
            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g.setPaint(gp);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, w/6.0, h/6.0));

            // Inner highlight
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(4, 4, w-8, h-8, w/8.0, h/8.0));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // Draw stylized "<X>" in center
            g.setColor(Color.WHITE);
            Font f = new Font("Segoe UI", Font.BOLD, Math.max(12, w/2));
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            String s = "<X>";
            int sw = fm.stringWidth(s);
            int sh = fm.getAscent();
            g.drawString(s, (w - sw) / 2, (h + sh) / 2 - 4);
        } finally {
            g.dispose();
        }
        return img;
    }

    // Sample data so the UI isn't empty when you start
    private String getSampleXslt() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
               "  <xsl:output method=\"xml\" indent=\"yes\"/>\n" +
               "\n" +
               "  <xsl:template match=\"/\">\n" +
               "    <html>\n" +
               "      <body>\n" +
               "        <h2>Personnes</h2>\n" +
               "        <table border=\"1\">\n" +
               "          <tr bgcolor=\"#9acd32\">\n" +
               "            <th>Name</th>\n" +
               "            <th>Age</th>\n" +
               "          </tr>\n" +
               "          <xsl:for-each select=\"people/person\">\n" +
               "            <tr>\n" +
               "              <td><xsl:value-of select=\"name\"/></td>\n" +
               "              <td><xsl:value-of select=\"age\"/></td>\n" +
               "            </tr>\n" +
               "          </xsl:for-each>\n" +
               "        </table>\n" +
               "      </body>\n" +
               "    </html>\n" +
               "  </xsl:template>\n" +
               "</xsl:stylesheet>";
    }

    private String getSampleXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<people>\n" +
               "  <person>\n" +
               "    <name>Alice</name>\n" +
               "    <age>30</age>\n" +
               "  </person>\n" +
               "  <person>\n" +
               "    <name>Bob</name>\n" +
               "    <age>25</age>\n" +
               "  </person>\n" +
               "</people>";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new XsltTransformerApp().setVisible(true);
            }
        });
    }
}