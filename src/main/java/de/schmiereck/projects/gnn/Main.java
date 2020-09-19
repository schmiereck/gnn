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
        final Character[] statusArr = new Character[] { null, '0', '1' };
        final RuleSet ruleSet = new RuleSet(statusArr);
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

        final List<Character> inputArr = allCombination.get(0);
        final List<Character> outputArr = allOutputList.get(0);

        final int maxCells = Math.max(inputArr.size(), outputArr.size());

        final Matrix matrix = createMatrix(ruleSet, inputArr, outputArr, maxCells);

        int addLineCnt = 0;

        while (true) {
            calcOutput(matrix);

            final int errors = calcFitness(matrix);
            System.out.printf("fitness errors: %d\n", errors);

            if (errors == 0) {
                printMatrix(matrix);
                break;
            }
            
            if (increaseToNextMatrix(matrix, statusArr) == true) {
                addLine(matrix, ruleSet, 0, maxCells);
                addLineCnt++;
                if (addLineCnt > 16) {
                    break;
                }
            }
        }
    }

    private static boolean increaseToNextMatrix(final Matrix matrix, final Character[] statusArr) {
        boolean ret = false;
        final ArrayList<Line> lineList = matrix.getLineList();

        int linePos = 0;
        int cellPos = 0;
        int rulePos = 0;
        while (true) {
            final Line line = lineList.get(linePos);
            final Cell cell = line.getCell(cellPos);
            final Rules rules = cell.getRules();
            final Rule rule = rules.getRule(rulePos);
            final Character status = rule.outputStatus;
            final int statusPos = findSignPos(statusArr, status);
            if (statusPos < (statusArr.length - 1)) {
                final Character nextStatus = statusArr[statusPos + 1];
                rule.outputStatus = nextStatus;
                break;
            } else {
                clearStatus(matrix, statusArr, linePos, cellPos, rulePos);
                if (rulePos < rules.getMaxRulePos()) {
                    rulePos++;
                } else {
                    rulePos = 0;
                    if (cellPos < line.getMaxCellPos()) {
                        cellPos++;
                    } else {
                        cellPos = 0;
                        if (linePos < (lineList.size() - 1)) {
                            linePos++;
                        } else {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static void clearStatus(final Matrix matrix, final Character[] statusArr) {
        final ArrayList<Line> lineList = matrix.getLineList();
        final int linePos = lineList.size() - 1;
        final Line line = lineList.get(linePos);
        int cellPos = line.getMaxCellPos();
        final Cell cell = line.getCell(cellPos);
        final Rules rules = cell.getRules();
        int rulePos = rules.getMaxRulePos();
        clearStatus(matrix, statusArr, linePos, cellPos, rulePos);
    }

    private static void clearStatus(final Matrix matrix, final Character[] statusArr, final int linePos, final int cellPos, final int rulePos) {
        final ArrayList<Line> lineList = matrix.getLineList();

        for (int lp = 0; lp < (linePos - 1); lp++) {
            final Line line = lineList.get(lp);
            for (int cp = 0; cp < line.getSize(); cp++) {
                final Cell cell = line.getCell(cp);
                final Rules rules = cell.getRules();
                for (int rp = 0; rp <= rules.getMaxRulePos(); rp++) {
                    final Rule rule = rules.getRule(rp);
                    rule.outputStatus =  statusArr[0];
                }
            }
        }
        final Line line = lineList.get(linePos);
        for (int cp = 0; cp < cellPos; cp++) {
            final Cell cell = line.getCell(cp);
            final Rules rules = cell.getRules();
            for (int rp = 0; rp < rulePos; rp++) {
                final Rule rule = rules.getRule(rp);
                rule.outputStatus =  statusArr[0];
            }
        }
    }

    private static int findSignPos(final Character[] statusArr, final Character status) {
        for (int pos = 0; pos < statusArr.length; pos++) {
            if (Objects.equals(statusArr[pos], status)) {
                return pos;
            }
        }
        throw new RuntimeException("Unknown Status \"" + status + "\".");
    }

    private static Matrix createMatrix(final RuleSet ruleSet, final List<Character> inputArr, final List<Character> outputArr, final int maxCells) {
        final Matrix matrix = new Matrix();

        {
            final Line inputLine = new Line();
            inputArr.forEach(c -> inputLine.addCell(new Cell(ruleSet, c)));
            matrix.setInputLine(inputLine);
        }
        addLine(matrix, ruleSet, 0, maxCells);
        {
            final Line outputLine = new Line();
            outputArr.forEach(c -> outputLine.addCell(new Cell(ruleSet, c)));
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
