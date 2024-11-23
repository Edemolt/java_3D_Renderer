package org.kushagra;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoViewer {

    public static Color getShade(Color color, double shade){
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout( new BorderLayout());

        // slider controls
        JSlider headingSlider = new JSlider(0, 360, 180);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // now for vertical controls
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        List<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));


        JPanel renderPanel = new JPanel(){
            public void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
//                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);

//                for(Triangle t : tris){
//                    Path2D path = new Path2D.Double();
//                    path.moveTo(t.v1.x, t.v1.y);
//                    path.lineTo(t.v2.x, t.v2.y);
//                    path.lineTo(t.v3.x, t.v3.y);
//                    path.closePath();
//                    g2.draw(path);
//                }

                double heading = Math.toRadians(headingSlider.getValue());
                double pitch = Math.toRadians(pitchSlider.getValue());

                Matrix headingTransform = new Matrix(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                Matrix pitchTransform = new Matrix(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });

                Matrix transform = headingTransform.multiply(pitchTransform);

//                Matrix transform = new Matrix(new double[]{
//                        Math.cos(heading), 0, -Math.sin(heading),
//                        0, 1, 0,
//                        Math.sin(heading), 0, Math.cos(heading)
//                });

//                g2.translate(getWidth()/2, getHeight()/2);
//                g2.setColor(Color.WHITE);


                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                double []zBuffer = new double[img.getWidth() * img.getHeight()];

                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

//                Vertex lightSource = new Vertex(0, 0, -1); // Pointing towards negative Z direction
                Vertex lightDirection = new Vertex(0, 0, 1); // Pointing towards positive Z direction
                for(Triangle t : tris){
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    // since we are not using Graphics2D anymore,
                    // we have to do translation manually
                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);

                    Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x
                    );

                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    if(normalLength != 0){
                        norm.x /= normalLength;
                        norm.y /= normalLength;
                        norm.z /= normalLength;
                    }

                    // calculate cosine of angle using z-component of normal and light source
                    double angleCose = Math.abs(norm.z); // Since light direction is [0, 0, 1]

                    Color shadedColor = getShade(t.color, angleCose);

                    // to calculate light intensity based on angle between normal and light source
//                    double dotproduct = norm.x * lightSource.x + norm.y * lightSource.y + norm.z * lightSource.z;

//                    double intensity = Math.max( dotproduct, 0);

                    // now applying shading, will multiply color with intensity
//                    Color shadedColor = new Color(
//                            (int) (t.color.getRed() * intensity),
//                            (int) (t.color.getGreen() * intensity),
//                            (int) (t.color.getBlue() * intensity)
//                    );

                    // compute rectangular bounds for triangle
                    // Rasterization
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));



                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                            if (b1 >= 0 && b2 >= 0 && b3 >= 0) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, shadedColor.getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }


//                            double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
//                            int zIndex = y * img.getWidth() + x;
//                            if (zBuffer[zIndex] < depth) {
//                                img.setRGB(x, y, t.color.getRGB());
//                                zBuffer[zIndex] = depth;
//                            }

//
//                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
//                                img.setRGB(x, y, t.color.getRGB());
//                            }
                        }
                    }


                }

                g2.drawImage(img, 0, 0, null);
            }
        };

        headingSlider.addChangeListener((e) -> {
            renderPanel.repaint();
        });

        pitchSlider.addChangeListener((e) -> {
            renderPanel.repaint();
        });
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);

    }
}
