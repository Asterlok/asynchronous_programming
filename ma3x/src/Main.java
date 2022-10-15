import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {

    /**
     * Вывод матрицы в файл.
     * Производится выравнивание значений для лучшего восприятия.
     **/
    private static void printMatrix(FileWriter fileWriter, int[][] matrix) throws IOException {
        boolean hasNegative = false;  // Признак наличия в матрице отрицательных чисел.
        int maxValue = 0;      // Максимальное по модулю число в матрице.

        // Вычисляем максимальное по модулю число в матрице и проверяем на наличие отрицательных чисел.
        for (final int[] row : matrix) {  // Цикл по строкам матрицы.
            for (final int element : row) {  // Цикл по столбцам матрицы.
                int temp = element;
                if (element < 0) {
                    hasNegative = true;
                    temp = -temp;
                }
                if (temp > maxValue) maxValue = temp;
            }
        }

        // Вычисление длины позиции под число.
        int len = Integer.toString(maxValue).length() + 1;  // Одно знакоместо под разделитель (пробел).
        if (hasNegative) ++len;  // Если есть отрицательные, добавляем знакоместо под минус.

        // Построение строки формата.
        final String formatString = "%" + len + "d";

        // Вывод элементов матрицы в файл.
        for (final int[] row : matrix) {
            for (final int element : row)
                fileWriter.write(String.format(formatString, element));

            fileWriter.write("\n");  // Разделяем строки матрицы переводом строки.
        }
    }

    private static void printAllMatrix(final String fileName, final int[][] firstMatrix, final int[][] secondMatrix, final int[][] resultMatrix) {
        try (final FileWriter fileWriter = new FileWriter(fileName, false)) {
            fileWriter.write("First matrix:\n");
            printMatrix(fileWriter, firstMatrix);

            fileWriter.write("\nSecond matrix:\n");
            printMatrix(fileWriter, secondMatrix);

            fileWriter.write("\nResult matrix:\n");
            printMatrix(fileWriter, resultMatrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Однопоточное умножение матриц.
     *
     * @param firstMatrix  Первая матрица.
     * @param secondMatrix Вторая матрица.
     * @return Результирующая матрица.
     */
    private static int[][] multiplyMatrix(final int[][] firstMatrix, final int[][] secondMatrix) {
        final int rowCount = firstMatrix.length;             // Число строк результирующей матрицы.
        final int colCount = secondMatrix[0].length;         // Число столбцов результирующей матрицы.
        final int sumLength = secondMatrix.length;           // Число членов суммы при вычислении значения ячейки.
        final int[][] result = new int[rowCount][colCount];  // Результирующая матрица.

        for (int row = 0; row < rowCount; ++row) {  // Цикл по строкам матрицы.
            for (int col = 0; col < colCount; ++col) {  // Цикл по столбцам матрицы.
                int sum = 0;
                for (int i = 0; i < sumLength; ++i)
                    sum += firstMatrix[row][i] * secondMatrix[i][col];
                result[row][col] = sum;
            }
        }

        return result;
    }

    /**
     * Многопоточное умножение матриц.
     *
     * @param firstMatrix  Первая (левая) матрица.
     * @param secondMatrix Вторая (правая) матрица.
     * @param threadCount  Число потоков.
     * @return Результирующая матрица.
     */
    private static int[][] multiplyMatrixMT(final int[][] firstMatrix, final int[][] secondMatrix, int threadCount) {

        int rows = firstMatrix.length;             // Число строк результирующей матрицы.
        int columns = secondMatrix[0].length;         // Число столбцов результирующей матрицы.
        int[][] result = new int[rows][columns];  // Результирующая матрица.

        // Жест справедливости!
        int cellsForThread = (rows * columns) / threadCount;  // Число вычисляемых ячеек на поток.
        int firstIndex = 0;  // Индекс первой вычисляемой ячейки.
        MultiplierThread[] mt = new MultiplierThread[threadCount];  // Массив потоков.

        // Создание и запуск потоков.
        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;  // Индекс последней вычисляемой ячейки.
            if (threadIndex == 0) {
                /* Один из потоков должен будет вычислить не только свой блок ячеек,
                   но и остаток, если число ячеек не делится нацело на число потоков. */
                lastIndex = rows * columns;
            }
            mt[threadIndex] = new MultiplierThread(firstMatrix, secondMatrix, result, firstIndex, lastIndex);
            mt[threadIndex].start();
            firstIndex = lastIndex;
        }

        // Ожидание завершения потоков.
        try {
            for (final MultiplierThread multiplierThread : mt)
                multiplierThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    static int FIRST_MATRIX_ROWS = 10;
    static int FIRST_MATRIX_COLS = 10;
    static int SECOND_MATRIX_ROWS = FIRST_MATRIX_COLS;
    final static int SECOND_MATRIX_COLS = 10;

    private static void randomMatrix(int[][] matrix) {
        Random random = new Random();
        for (int row = 0; row < matrix.length; ++row)
            for (int col = 0; col < matrix[row].length; ++col)
                matrix[row][col] = random.nextInt(100);
    }

    public static void main(String[] args) {
        int[][] firstMatrix = new int[FIRST_MATRIX_ROWS][FIRST_MATRIX_COLS];
        int[][] secondMatrix = new int[SECOND_MATRIX_ROWS][SECOND_MATRIX_COLS];

        randomMatrix(firstMatrix);
        randomMatrix(secondMatrix);

        long timeMultiThreads = System.nanoTime();
        int[][] resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());
        timeMultiThreads = System.nanoTime() - timeMultiThreads;
        System.out.println("timeMultiThreads");
        System.out.printf("Elapsed %,9.3f ms\n", timeMultiThreads / 1_000_000.0);

        long timeSingleThread = System.nanoTime();
        multiplyMatrix(firstMatrix, secondMatrix);
        timeSingleThread = System.nanoTime() - timeSingleThread;
        System.out.println("timeSingleThread");
        System.out.printf("Elapsed %,9.3f ms\n", timeSingleThread / 1_000_000.0);


        printAllMatrix("Matrix.txt", firstMatrix, secondMatrix, resultMatrixMT);
    }
}