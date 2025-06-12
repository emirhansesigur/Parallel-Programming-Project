import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

public class IsInside {
    public static boolean isInsideSerial(List<double[]> edges, double xp, double yp) {
        int cnt = 0;
        for (double[] edge : edges) {
            double x1 = edge[0], y1 = edge[1], x2 = edge[2], y2 = edge[3];
            if ((yp < y1) != (yp < y2) && xp < x1 + ((yp - y1) / (y2 - y1)) * (x2 - x1)) {
                cnt++;
            }
        }
        System.out.println("Serial CNT VALUE: "+cnt);
        return cnt % 2 == 1;
    }

    public static boolean isInsideCallable(List<double[]> edges, double xp, double yp) {
        long threadPoolStart = System.nanoTime();
        int maxThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Callable<Integer>> tasks = new ArrayList<>();
        // Her edge için ayrı bir Callable oluştur
        for (double[] edge : edges) {
            tasks.add(() -> {
                double x1 = edge[0], y1 = edge[1], x2 = edge[2], y2 = edge[3];
                if ((yp < y1) != (yp < y2) && xp < x1 + ((yp - y1) / (y2 - y1)) * (x2 - x1)) {
                    return 1; // Kesişim var
                } else {
                    return 0; // Kesişim yok
                }
            });
        }
        long threadPoolEnd = System.nanoTime();
        System.out.println("Thread pool ve task oluşturma süresi: " + (threadPoolEnd - threadPoolStart) / 1_000_000.0 + " ms");

        int cnt = 0;
        try {
            long invokeStart = System.nanoTime();
            // Tüm task'ları çalıştır ve sonuçları topla
            List<Future<Integer>> results = executor.invokeAll(tasks);
            long invokeEnd = System.nanoTime();
            for (Future<Integer> result : results) {
                cnt += result.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        System.out.println("Callable CNT VALUE: " + cnt);
        return cnt % 2 == 1;
    }

    public static void drawPolygonAndPoint(List<double[]> points, double xp, double yp, boolean isInside) {
        JFrame frame = new JFrame("Poligon ve Nokta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int margin = 50;
                double minX = points.stream().mapToDouble(p -> p[0]).min().orElse(0);
                double minY = points.stream().mapToDouble(p -> p[1]).min().orElse(0);
                double maxX = points.stream().mapToDouble(p -> p[0]).max().orElse(1);
                double maxY = points.stream().mapToDouble(p -> p[1]).max().orElse(1);
                double scaleX = (getWidth() - 2 * margin) / (maxX - minX);
                double scaleY = (getHeight() - 2 * margin) / (maxY - minY);
                int n = points.size();
                int[] xs = new int[n];
                int[] ys = new int[n];
                for (int i = 0; i < n; i++) {
                    xs[i] = (int) ((points.get(i)[0] - minX) * scaleX) + margin;
                    ys[i] = getHeight() - ((int) ((points.get(i)[1] - minY) * scaleY) + margin);
                }
                // Eksenleri çiz
                g2.setColor(Color.LIGHT_GRAY);
                // X ekseni
                int y0 = getHeight() - ((int) ((0 - minY) * scaleY) + margin);
                g2.drawLine(margin, y0, getWidth() - margin, y0);
                // Y ekseni
                int x0 = (int) ((0 - minX) * scaleX) + margin;
                g2.drawLine(x0, margin, x0, getHeight() - margin);
                // Eksenlere değerler yaz
                g2.setColor(Color.DARK_GRAY);
                for (int i = 0; i <= 10; i++) {
                    double xVal = minX + i * (maxX - minX) / 10;
                    int xPix = (int) ((xVal - minX) * scaleX) + margin;
                    g2.drawLine(xPix, getHeight() - margin - 5, xPix, getHeight() - margin + 5);
                    g2.drawString(String.format("%.1f", xVal), xPix - 10, getHeight() - margin + 20);
                }
                for (int i = 0; i <= 10; i++) {
                    double yVal = minY + i * (maxY - minY) / 10;
                    int yPix = getHeight() - ((int) ((yVal - minY) * scaleY) + margin);
                    g2.drawLine(margin - 5, yPix, margin + 5, yPix);
                    g2.drawString(String.format("%.1f", yVal), margin - 40, yPix + 5);
                }
                // Poligon çiz
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.drawPolyline(xs, ys, n);
                // Noktayı çiz
                int px = (int) ((xp - minX) * scaleX) + margin;
                int py = getHeight() - ((int) ((yp - minY) * scaleY) + margin);
                g2.setColor(isInside ? Color.GREEN : Color.RED);
                g2.fillOval(px - 6, py - 6, 12, 12);
            }
        };
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Poligonun kaç kenarı olacak? ");
        int n = Integer.parseInt(scanner.nextLine());
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print((i + 1) + ". noktanın x ve y koordinatlarını aralarına boşluk koyarak girin: ");
            String[] xy = scanner.nextLine().trim().split(" ");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            points.add(new double[]{x, y});
        }
        // Poligonu kapat
        points.add(points.get(0));
        // Kenarları oluştur
        List<double[]> edges = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            double[] p1 = points.get(i);
            double[] p2 = points.get(i + 1);
            edges.add(new double[]{p1[0], p1[1], p2[0], p2[1]});
        }
        System.out.print("Test etmek istediğiniz noktanın x ve y koordinatlarını girin: ");
        String[] xy = scanner.nextLine().trim().split(" ");
        double xp = Double.parseDouble(xy[0]);
        double yp = Double.parseDouble(xy[1]);

        // isInsideSerial zaman ölçümü
        long startTimeSerial = System.nanoTime();
        boolean insideSerial = isInsideSerial(edges, xp, yp);
        long endTimeSerial = System.nanoTime();
        double elapsedMsSerial = (endTimeSerial - startTimeSerial) / 1_000_000.0;
        System.out.println("isInsideSerial fonksiyonu " + elapsedMsSerial + " ms sürdü.");
        if (insideSerial) {
            System.out.println("Serial: inside");
        } else {
            System.out.println("Serial: outside");
        }
        // isInsideCallable zaman ölçümü
        long startTimeCallable = System.nanoTime();
        boolean insideCallable = isInsideCallable(edges, xp, yp);
        long endTimeCallable = System.nanoTime();
        double elapsedMsCallable = (endTimeCallable - startTimeCallable) / 1_000_000.0;
        System.out.println("isInsideCallable fonksiyonu " + elapsedMsCallable + " ms sürdü.");
        if (insideCallable) {
            System.out.println("Callable: inside");
        } else {
            System.out.println("Callable: outside");
        }
        drawPolygonAndPoint(points, xp, yp, insideSerial);
        scanner.close();
    }
}
