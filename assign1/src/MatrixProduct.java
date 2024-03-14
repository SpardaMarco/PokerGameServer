package src;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MatrixProduct {
 
    static File file = new File("javameasurements.txt");
    static FileWriter writer;
    
    void onMult(int lin, int col) throws IOException {
        double temp;
        int i, j, k;
        double[] pha = new double[lin * col];
        double[] phb = new double[lin * col];
        double[] phc = new double[lin * col];

        for (i = 0; i < lin; i++)
            for (j = 0; j < lin; j++)
                pha[i * lin + j] = (double) 1.0;

        for (i = 0; i < col; i++)
            for (j = 0; j < col; j++)
                phb[i * col + j] = (double) (i + 1);

        long start = System.nanoTime();
        for (i = 0; i < lin; i++)
            for (j = 0; j < col; j++) {
                temp = 0.0;
                for (k = 0; k < lin; k++)
                    temp += pha[i * lin + k] * phb[k * col + j];
                phc[i * lin + j] = temp;
            }
        
        long end = System.nanoTime();
        System.out.print("Dimensions: " + lin + 'x' + col + "\n");
        writer.write("Dimensions: " + lin + 'x' + col + "\n");
        System.out.print("Time: " + (end - start)/1000000000.0 + " s\n");
        writer.write("Time: " + (end - start)/1000000000.0 + " s\n");
        System.out.print("Result matrix: \n");
        writer.write("Result matrix: \n");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10,col); j++) {
                System.out.print(phc[j] + " ");
                writer.write(phc[j] + " ");
            }
        }

        System.out.print("\n");
        writer.write("\n\n");
        return;
    }

    void onMultLine(int lin, int col) throws IOException {
        double temp;
        int i, j, k;
        double[] pha = new double[lin * col];
        double[] phb = new double[lin * col];
        double[] phc = new double[lin * col];

        for (i = 0; i < lin; i++)
            for (j = 0; j < lin; j++)
                pha[i * lin + j] = (double) 1.0;

        for (i = 0; i < col; i++)
            for (j = 0; j < col; j++)
                phb[i * col + j] = (double) (i + 1);

        for (i = 0; i < lin; i++)
            for (j = 0; j < col; j++)
                phc[i * lin + j] = 0.0;

        long start = System.nanoTime();
        for (i = 0; i < lin; i++)
            for (k = 0; k < col; k++) {
                for (j = 0; j < lin; j++)
                    phc[i * lin + j] += pha[i * lin + k] * phb[k * col + j];
            }

        long end = System.nanoTime();
        System.out.print("Dimensions: " + lin + 'x' + col + "\n");
        writer.write("Dimensions: " + lin + 'x' + col + "\n");
        System.out.print("Time: " + (end - start)/1000000000.0 + " s\n");
        writer.write("Time: " + (end - start)/1000000000.0 + " s\n");
        System.out.print("Result matrix: \n");
        writer.write("Result matrix: \n");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10,col); j++) {
                System.out.print(phc[j] + " ");
                writer.write(phc[j] + " ");
            }
        }

        writer.write("\n\n");
        System.out.print("\n");
        return;
    }

    public static void main(String[] args) throws IOException {
        int op = 0;
        int blockSize = 0;
        int start = 0;
        int end = 0;
        int step = 0;
        int repetions = 0;

        if (!file.exists()) {
            if (!file.createNewFile()) {
                System.out.print("Error creating file\n");
                return;
            }
        }

        writer = new FileWriter(file, true);

        do {
            Scanner sc = new Scanner(System.in);
            MatrixProduct mp = new MatrixProduct();
            System.out.print("1. Multiplication\n");
            System.out.print("2. Line Multiplication\n");
            System.out.print("3. Block Multiplication\n");
            System.out.print("0. Exit\n");
            System.out.print("Option: ");
            op = sc.nextInt();

            if (op != 0) {
                switch (op) {
                    case 1:
                        writer.write("Multiplication\n\n");
                        System.out.print("Starting dimensions 'line=cols' (600): ");
                        start = sc.nextInt();
                        System.out.print("Ending dimensions 'line=cols' (3000): ");
                        end = sc.nextInt();
                        System.out.print("Step (400): ");
                        step = sc.nextInt();
                        System.out.print("Repetions (3): ");
                        repetions = sc.nextInt();
                        for (int j = 0; j < repetions; j++){
                            System.out.print("Repetion " + j + '\n');
                            writer.write("Repetion " + j + '\n');
                            for (int i = start; i <= end; i += step) {
                                System.out.print('\n');
                                mp.onMult(i, i);
                            }
                            writer.write("\n");
                        }
                        System.out.print('\n');
                        break;
                    case 2:
                        writer.write("Line Multiplication\n\n");
                        System.out.print("Starting dimensions 'line=cols' (600): ");
                        start = sc.nextInt();
                        System.out.print("Ending dimensions 'line=cols' (3000): ");
                        end = sc.nextInt();
                        System.out.print("Step (400): ");
                        step = sc.nextInt();
                        System.out.print("Repetions (3): ");
                        repetions = sc.nextInt();
                        for (int j = 0; j < repetions; j++){
                            writer.write("Repetion " + j + '\n');
                            for (int i = start; i <= end; i += step) {
                                System.out.print('\n');
                                mp.onMultLine(i, i);
                            }
                            writer.write("\n");
                        }
                        System.out.print('\n');
                        break;
                    case 3:
                    {
                        writer.write("Block Multiplication\n\n");
                        System.out.print("Block size (64 128 256 512 1024): \n");
                        sc.nextLine();
                        String[] blockSizes = sc.nextLine().split(" ");
                        System.out.print("Starting dimensions 'line=cols' (4096): ");
                        start = sc.nextInt();
                        System.out.print("Ending dimensions 'line=cols' (10240): ");
                        end = sc.nextInt();
                        System.out.print("Step (2048): ");
                        step = sc.nextInt();
                        System.out.print("Repetions (3): ");
                        repetions = sc.nextInt();
                        for (int j = 0; j < repetions; j++){
                            writer.write("Repetion " + j + '\n');
                            for (String blockSizeStr : blockSizes) {
                                blockSize = Integer.parseInt(blockSizeStr);
                                for (int i = start; i <= end; i += step) {
                                    System.out.print('\n');
                                    mp.onMultBlock(i, i, blockSize);
                                }
                            }
                            writer.write("\n");
                        }

                        System.out.print('\n');
                        break;
                    }
                    default:
                        System.out.print("Invalid option\n");
                        break;
                }
            }
        } while (op != 0);
        
        writer.close();
    }
}
