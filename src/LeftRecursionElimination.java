import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class LeftRecursionElimination {
    public static final String FILE_PATH = "src/test.txt";
    public static final String EPSILON = "$";

    private static List<String> readLines(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).collect(Collectors.toList());
    }

    private static Map<String, Set<String>> parseGrammar(String grammar) {
        Map<String, Set<String>> parsedGrammar = new LinkedHashMap<>();
        String[] splittedString = grammar.split("\n");
        String[] splittedRule;
        for (String currentRule : splittedString) {
            splittedRule = currentRule.replaceAll(" ", "").split("->");
            parsedGrammar.put(splittedRule[0], new CopyOnWriteArraySet<>((Arrays.asList(splittedRule[1].split("\\|")))));
        }
        return parsedGrammar;
    }

    private static String stringifyGrammar(Map<String, Set<String>> parsedGrammar) {
        String currentSymbol;
        Set<String> currentDefinition;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> currentRule : parsedGrammar.entrySet()) {
            currentSymbol = currentRule.getKey();
            currentDefinition = currentRule.getValue();
            sb.append(currentSymbol).append(" -> ");
            for (String symbols : currentDefinition) {
                sb.append(symbols).append(" | ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void eliminateEpsilonProductions(Map<String, Set<String>> parsedGrammar) {
        String currentSymbol;
        Set<String> currentDefinition;
        Set<String> epsilonProductions = new HashSet<>();
        Set<Map.Entry<String, Set<String>>> productions = parsedGrammar.entrySet();
        for (Map.Entry<String, Set<String>> currentRule : productions) {
            currentSymbol = currentRule.getKey();
            currentDefinition = currentRule.getValue();
            if (currentDefinition.contains(EPSILON)) {
                epsilonProductions.add(currentSymbol);
                currentDefinition.remove(EPSILON);
            }
        }
        for (Map.Entry<String, Set<String>> currentRule : productions) {
            currentDefinition = currentRule.getValue();
            for (String epsilonSymbol : epsilonProductions) {
                for (String symbols : currentDefinition) {
                    if (symbols.contains(epsilonSymbol)) {
                        currentDefinition.add(symbols.replace(epsilonSymbol, ""));
                    }
                }
            }
        }
    }

    public static String eliminate(String leftRecursiveGrammar) {
        Map<String, Set<String>> parsedGrammar = parseGrammar(leftRecursiveGrammar);
        eliminateEpsilonProductions(parsedGrammar);
        ArrayList<String> keys = new ArrayList<>();
        for (String s : parsedGrammar.keySet()) {
            keys.add(s);
        }
        Set<String> newRuleSet;
        Set<String> newRuleSet1;
        Set<String> newRuleSet2;
        for (int i = 0; i < parsedGrammar.size(); i++) {
            for (int j = 0; j < i; j++) {
                newRuleSet = new HashSet<>();
                String currentRule = "";
                for (String rule : parsedGrammar.get(keys.get(i))) {
                    if (!rule.endsWith("\'") && rule.startsWith(keys.get(j))) {
                        currentRule = rule;
                        for (String s : parsedGrammar.get(keys.get(j))) {
                            newRuleSet.add(s + rule.substring(keys.get(j).length()));
                        }
                        if (!currentRule.equals("")) {
                            parsedGrammar.get(keys.get(i)).remove(currentRule);
                            parsedGrammar.get(keys.get(i)).addAll(newRuleSet);
                        }
                    }
                }

            }
            newRuleSet1 = new HashSet<>();
            newRuleSet2 = new HashSet<>();
            for (String s : parsedGrammar.get(keys.get(i))) {
                if (s.startsWith(keys.get(i))) {
                    newRuleSet2.add(s.substring(keys.get(i).length()) + keys.get(i) + "\'");
                } else {
                    newRuleSet1.add(s + keys.get(i) + "\'");
                }
            }

            if (!(newRuleSet2.isEmpty() || newRuleSet1.isEmpty())) {
                newRuleSet2.add("$");
                parsedGrammar.remove(keys.get(i));
                parsedGrammar.put(keys.get(i), newRuleSet1);
                parsedGrammar.put(keys.get(i) + "\'", newRuleSet2);
                keys.add(keys.get(i) + "\'");
            }

        }
        return stringifyGrammar(parsedGrammar);
    }

    private static void runTests() throws IOException {
        String grammar = "";
        for (String line : readLines(FILE_PATH)) {
            if (line.equals("")) {
                System.out.println(eliminate(grammar));
                grammar = "";
            } else {
                grammar += line + "\n";
            }
        }
    }

    public static void main(String[] args) {
        try {
            PrintStream o = new PrintStream(new File("src/result.txt"));
            System.setOut(o);
            runTests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
