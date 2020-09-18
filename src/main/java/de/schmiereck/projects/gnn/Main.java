package de.schmiereck.projects.gnn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.schmiereck.projects.gnn.utils.Combinations;
import de.schmiereck.projects.gnn.utils.KPermutations;
import de.schmiereck.projects.gnn.utils.Permutations;

public class Main {
    public static void main(final String[] args) {
        System.out.println("gnn V1.0.0");

        final Character[] inputSigns = new Character[] { '0', '1' };
        final Character[] signs = new Character[] { null, '0', '1' };
        final RuleSet ruleSet = new RuleSet(signs);
        final List<List<Character>> allOutputList = new ArrayList<>();

        //final Combination<Character> signCombinations = new Combination(Arrays.asList(inputSigns), 2);
        //for (final List<Character> signCombination : signCombinations) {
        //    System.out.println(signCombination);
        //}

        final List<List<Character>> allCombination = ListUtils.getAllCombination(inputSigns, 2);
        for (int pos = 0; pos < allCombination.size(); pos++) {
            System.out.println(allCombination.get(pos));
        }

        for (int pos = 0; pos < (2*2); pos++) {
            final List<Character> outputList = new ArrayList<>();
            for (int oPos = 0; oPos < (2*2); oPos++) {
                if (pos == oPos) {
                    outputList.add(Character.valueOf('1'));
                } else {
                    outputList.add(Character.valueOf('0'));
                }
            }
            System.out.println(outputList);
            allOutputList.add(outputList);
        }

        final Character[] inputArr = new Character[]{
                '0',
                null,
                '1'
        };
        final Character[] outputArr = new Character[]{
                '1',
                null,
                null
        };

        final Matrix matrix = createMatrix(ruleSet, inputArr, outputArr);

        calcOutput(matrix);

        printMatrix(matrix);

        final int errors = calcFitness(matrix);
        System.out.printf("fitness errors: %d\n", errors);
    }

    private static Matrix createMatrix(final RuleSet ruleSet, final Character[] inputArr, final Character[] outputArr) {
        final Matrix matrix = new Matrix();

        {
            final Line inputLine = new Line();
            Arrays.stream(inputArr).forEach(c -> inputLine.addCell(new Cell(ruleSet, c)));
            matrix.setInputLine(inputLine);
        }
        addLine(matrix, ruleSet, 0, 3);
        {
            final Line outputLine = new Line();
            Arrays.stream(outputArr).forEach(c -> outputLine.addCell(new Cell(ruleSet, c)));
            matrix.setOutputLine(outputLine);
        }
        return matrix;
    }

    private static void addLine(final Matrix matrix, final RuleSet ruleSet, final int linePos, final int cellCount) {
        final Line line = new Line();

        for (int pos = 0; pos < cellCount; pos++) {
            final Cell cell = new Cell(ruleSet);
            line.addCell(cell);
        }
        matrix.addLine(linePos, line);
    }

    private static void printMatrix(final Matrix matrix) {
        System.out.println("==================");
        printLine(matrix.getInputLine());
        System.out.println("------------------");
        matrix.getLineList().stream().forEach(line -> {
            printLine(line);
        });
        System.out.println("------------------");
        printLine(matrix.getOutputLine());
    }

    private static void printLine(final Line line) {
        line.getCellList().stream().forEach(cell -> {
            final Character status = cell.getStatus();
            if (Objects.nonNull(status)) {
                System.out.print(status);
            } else {
                System.out.print(".");
            }
        });
        System.out.println();
    }

    private static void calcOutput(final Matrix matrix) {
        matrix.getLineList().stream().forEach(line -> calcOutput(matrix, line));
    }

    private static void calcOutput(final Matrix matrix, final Line line) {
        line.getCellList().stream().forEach(cell -> calcOutput(matrix, line, cell));
    }

    private static void calcOutput(final Matrix matrix, final Line line, final Cell cell) {
        final Cell[] inputCells = new Cell[3];

        inputCells[0] = matrix.getCell(line.getPos() - 1, cell.getPos() - 1);
        inputCells[1] = matrix.getCell(line.getPos() - 1, cell.getPos());
        inputCells[2] = matrix.getCell(line.getPos() - 1, cell.getPos() + 1);

        final Rules rules = cell.getRules();

        final Rule rule = rules.findRule(inputCells[0].getStatus(), inputCells[1].getStatus(), inputCells[2].getStatus());

        if (Objects.nonNull(rule)) {
            cell.setStatusRule(rule);
        }
    }

    private static int calcFitness(final Matrix matrix) {
        final Line outputLine = matrix.getOutputLine();
        final ArrayList<Line> lineList = matrix.getLineList();
        final Line lastLine = lineList.get(lineList.size() - 1);

        final int retError =
                outputLine.getCellList().stream().
                        mapToInt(outputCell -> calcFitness(matrix, lastLine.getCell(outputCell.getPos()), outputCell)).
                        sum();

        return retError;
    }

    private static int calcFitness(final Matrix matrix, final Cell lastCell, final Cell outputCell) {
        final int retError;
        final Character lastCellStatus = lastCell.getStatus();
        final Character outputCellStatus = outputCell.getStatus();

        if (Objects.equals(lastCellStatus, outputCellStatus)) {
            retError = 0;
            lastCell.getStatusRule().outputStatus = outputCellStatus;
        } else {
            retError = 1;
        }
        return retError;
    }
}
