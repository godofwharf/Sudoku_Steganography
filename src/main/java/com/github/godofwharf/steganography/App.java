package com.github.godofwharf.steganography;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * @author godofwharf
 */
public class App extends JFrame implements ActionListener {

    private JButton encryptButton, sendButton, viewButton;

    private JTextField tFileName, tMessage, tSudoku;

    private URL imageUrl, sudokuUrl;

    private int Width, Height;

    private BufferedImage originalImage, modifiedImage;

    private static JFrame mainframe;

    public App() throws Exception {
        super("App");
        JLabel lFileName = new JLabel("File Url:");
        JLabel lMessage = new JLabel("Secret Message:");
        JLabel lSudoku = new JLabel("Sudoku Url:");

        encryptButton = new JButton("encryptButton");
        sendButton = new JButton("sendButton");
        viewButton = new JButton("viewButton");

        tFileName = new JTextField("", 40);
        tMessage = new JTextField("", 40);
        tSudoku = new JTextField("", 40);

        JPanel pFileName = new JPanel();
        JPanel pMessage = new JPanel();
        JPanel pSudoku = new JPanel();
        JPanel pButtons = new JPanel();

        Container container = getContentPane();
        BoxLayout layout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(layout);

        pFileName.add(lFileName);
        pFileName.add(tFileName);
        pMessage.add(lMessage);
        pMessage.add(tMessage);
        pSudoku.add(lSudoku);
        pSudoku.add(tSudoku);
        pButtons.add(encryptButton);
        pButtons.add(viewButton);
        pButtons.add(sendButton);

        container.add(pFileName);
        container.add(pMessage);
        container.add(pSudoku);
        container.add(pButtons);

        sendButton.addActionListener(this);
        encryptButton.addActionListener(this);
        viewButton.addActionListener(this);
    }

    public static void main(String[] args) throws Exception {
        App frame = new App();
        frame.setSize(800, 600);
        frame.setVisible(true);

        mainframe = frame;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private Encoder getEncoder() throws Exception {
        int[][] M = new int[27][27];
        int[][] sudoku = new int[9][9];
        BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(sudokuUrl.toURI()))));
        String st;
        int r, c;
        r = 0;
        while ((st = br.readLine()) != null) {
            c = 0;
            String[] tokens = st.split(" ");
            for (int i = 0; i < tokens.length; i++)
                sudoku[r][c++] = Integer.parseInt(tokens[i]);
            r++;
        }
        for (int i = 0; i < 27; i++)
            for (int j = 0; j < 27; j++)
                M[i][j] = sudoku[i % 9][j % 9] - 1;
        br.close();
        return new Encoder(sudoku, M);
    }

    private Decoder getDecoder() throws Exception {
        int[][] M = new int[27][27];
        int[][] sudoku = new int[9][9];
        BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(sudokuUrl.toURI()))));
        String st;
        int r, c;
        r = 0;
        while ((st = br.readLine()) != null) {
            c = 0;
            String[] tokens = st.split(" ");
            for (int i = 0; i < tokens.length; i++)
                sudoku[r][c++] = Integer.parseInt(tokens[i]);
            r++;
        }
        for (int i = 0; i < 27; i++)
            for (int j = 0; j < 27; j++)
                M[i][j] = sudoku[i % 9][j % 9] - 1;
        br.close();
        return new Decoder(M);
    }

    private int[] getImage() throws Exception {
        BufferedImage img;
        img = ImageIO.read(Files.newInputStream(Paths.get(imageUrl.toURI())));
        Width = img.getWidth();
        Height = img.getHeight();
        return img.getRGB(0, 0, Width, Height, null, 0, Width);
    }

    public void createView() throws Exception {
        JFrame frame = new JFrame("originalImage and modified images");
        frame.setSize(800, 600);
        frame.setVisible(true);

        Container container = frame.getContentPane();
        container.setLayout(new FlowLayout());

        JLabel lOrg = new JLabel(new ImageIcon(originalImage));
        JLabel lMod = new JLabel(new ImageIcon(modifiedImage));
        JPanel pImages = new JPanel();
        pImages.add(lOrg);
        pImages.add(lMod);
        container.add(pImages);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                mainframe.setVisible(true);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == viewButton) {
            try {
                sudokuUrl = new URL(tSudoku.getText());
                imageUrl = new URL(tFileName.getText());
                if (!sudokuUrl.getProtocol().equalsIgnoreCase("file") || !imageUrl.getProtocol().equalsIgnoreCase("file")) {
                    throw new RuntimeException("Only file scheme is supported for URLs");
                }
                Encoder encoder = getEncoder();
                int[] img = getImage();
                int[] modifiedImg = encoder.encode(tMessage.getText());

                Decoder decoder = getDecoder();
                String decodedMessage = decoder.decode(modifiedImg);
                if (!decodedMessage.equals(tMessage.getText())) {
                    throw new RuntimeException("Something went wrong, decoding is not successful");
                }

                originalImage = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
                originalImage.setRGB(0, 0, Width, Height, img, 0, Width);

                modifiedImage = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
                modifiedImage.setRGB(0, 0, Width, Height, modifiedImg, 0, Width);

                createView();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
