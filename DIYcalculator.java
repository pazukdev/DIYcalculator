import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pazuk on 12.02.2018.
 */

public class DIYcalculator {

    private static String expression;
    private static ArrayList<String> expressionAsArrayList;
    private  static  String result;

    private static boolean errorDetected = false;
    private static Scanner scanner;

    private static void inputBasicCheck(String expression) { // at first - check inputted expression for
                                                             // some elementary syntax errors

        Pattern patternValidCharacters=Pattern.compile("^[0-9\\s\\(\\)\\*\\+\\.,^/-]");
        Pattern patternOperandsOnly=Pattern.compile("^[0-9\\s\\.,]");
        Pattern patternOperatorsOnly=Pattern.compile("^[\\s\\(\\)\\*\\+\\.,^/-]");
        Matcher matcher;

        String[] expressionArray=expression.split("");

        int digitCounter=0;
        int operatorCounter=0;
        int openBracketCounter=0;
        int closeBracketCounter=0;

        int i=0;
        while (i<expressionArray.length) {
            matcher=patternValidCharacters.matcher(expressionArray[i]);
            if(!matcher.matches()) {
                System.out.println("Wrong expression input. Not allowed character detected: "+expressionArray[i]);
                errorDetected=true;
                return;
            }
            matcher=patternOperandsOnly.matcher(expressionArray[i]);
            if(matcher.matches()) {
                digitCounter++;
            }

            matcher=patternOperatorsOnly.matcher(expressionArray[i]);
            if(matcher.matches()) {
                operatorCounter++;
            }

            if(expressionArray[i].equals("(")) {
                openBracketCounter++;
            }

            if(expressionArray[i].equals(")")) {
                closeBracketCounter++;
            }

            i++;
        }

        if(digitCounter==expression.length()) {
            System.out.println("This is an incomplete mathematical expression: no operators");
            errorDetected=true;
            return;
        }

        if(operatorCounter==expression.length()) {
            System.out.println("This is incomplete mathematical expression: no operands");
            errorDetected=true;
            return;
        }

        if(openBracketCounter!=closeBracketCounter) {
            System.out.println("Error in the expression: wrong number of brackets " +
                    "\nCheck if the brackets are positioned correctly");
            errorDetected=true;
            return;
        }

        i=0;
        while (i<expression.length()) {
            if(expression.charAt(i)=='(' || expression.charAt(i)==')') { // check if the first bracket in...
                if(expression.charAt(i)==')') { // ...extension is not close bracket
                    System.out.println("Error in the expression: first bracket shouldn't be closing " +
                            "\ncheck if the brackets are positioned correctly");
                    errorDetected=true;
                    return;
                }
                break;

            }
            i++;
        }

        if(expression.length()<3) {
            System.out.println("This is incomplete mathematical expression");
            errorDetected=true;
        }

    }

    private static boolean digitFinder(String s) {
        String[] stringList=s.split("");
        for(int i=0; i<s.length(); i++) {
            if(stringList[i].matches("[0-9]")) {
                return true;
            }
        }
        return false;
    }


    private static String modifyExpression(String expression) { // change inputed expression to the form convenient for
                                                              // further processing and evaluating with shunting yard
                                                              // method, postfix notation and postfix evaluation
                                                              // algorithm
        expression=expression.replace(',', '.');

        expressionAsArrayList =new ArrayList<>();

        int u=0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                expressionAsArrayList.add(u, Character.toString(c));
                u++;
            }

            else if (c == ')') {
                expressionAsArrayList.add(u, Character.toString(c));
                u++;
            }

            else if (isOperator(c)) {
                expressionAsArrayList.add(u, Character.toString(c));
                u++;
            } else {

                String operand = "";

                while (i < expression.length() && expression.charAt(i)!='(' && expression.charAt(i)!=')'
                        && !isOperator(expression.charAt(i))) {
                    operand=operand+expression.charAt(i);
                    i++;
                }
                i--;

                expressionAsArrayList.add(u, operand);
                u++;

            }

        }

        // System.out.println(expressionAsArrayList); // - show parsed string intermediate stage

        for(int i=0; i<expressionAsArrayList.size(); i++) {
            String s=expressionAsArrayList.get(i);
            if(digitFinder(s)) {
                if(s.charAt(0)==' ') {
                    s=s.substring(1);
                }
                if(s.charAt(s.length()-1)==' ') {
                    s=s.substring(0, s.length()-1);
                }
                expressionAsArrayList.set(i, s);
            } else {
                s=s.replace(" ", "");
                expressionAsArrayList.set(i, s);
            }
        }

        for(int i=0; i<expressionAsArrayList.size(); i++) {
            String s = expressionAsArrayList.get(i);
            if (s.equals("")) {
                expressionAsArrayList.remove(i);
            }
        }

        if(expressionAsArrayList.get(0).equals("-")) {
            expressionAsArrayList.set(1, "-"+expressionAsArrayList.get(1));
            expressionAsArrayList.remove(0);
        }

        for(int i=2; i<expressionAsArrayList.size(); i++) {
            String s0=expressionAsArrayList.get(i-2);
            String s1=expressionAsArrayList.get(i-1);
            String s2=expressionAsArrayList.get(i);

            if(s1.equals("-") && (isBracket(s0.charAt(0)) || isOperator(s0.charAt(0)))) {
                s1=s1+s2;
                expressionAsArrayList.set(i-1, s1);
                expressionAsArrayList.remove(i);
            }
        }

        return expression;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private static boolean isBracket(char c) {
        return c=='(' || c==')';
    }


    private static int priority(char operator) {
        switch (operator) {
            case '+':
                return 1;
            case '-':
                return 1;

            case '*':
                return 2;
            case '/':
                return 2;

            case '^':
                return 3;

            default:
                return -1;
        }
    }


    private static void processOperator(LinkedList<String> numbersStack, char operator) {
        BigDecimal topNumber=new BigDecimal(numbersStack.removeLast());
        BigDecimal nextTopNumber=new BigDecimal(numbersStack.removeLast());
        //Double topNumber = numbersStack.removeLast();
        //Double nextTopNumber = numbersStack.removeLast();
        switch (operator) {
            case '+':
                numbersStack.add(String.valueOf(nextTopNumber.add(topNumber)));
                break;
            case '-':
                numbersStack.add(String.valueOf(nextTopNumber.subtract(topNumber)));
                break;
            case '*':
                numbersStack.add(String.valueOf(nextTopNumber.multiply(topNumber)));
                break;
            case '/':
                double d=topNumber.doubleValue();
                if(d==0) {
                    System.out.println("Dividing by zero impossible");
                    errorDetected=true;
                    break;
                }
                numbersStack.add(String.valueOf(nextTopNumber.divide(topNumber, 16, BigDecimal.ROUND_HALF_UP)));
                break;
            case '^':
                double d1=topNumber.doubleValue();
                double d2=nextTopNumber.doubleValue();
                numbersStack.add(String.valueOf(Math.pow(d2, d1)));
                break;
        }
    }


    private static String evaluate(ArrayList<String> expressionAsArrayList) { // calculate with railway shunting yard method
                                                                              // & reverse Polish notation

        if(errorDetected) {
            return "0";
        }
        System.out.println("Parsed expression: "+expressionAsArrayList); // parsed input final stage

        LinkedList<String> operandsStack = new LinkedList<>();
        LinkedList<Character> operatorsStack = new LinkedList<>();
        for (int i = 0; i < expressionAsArrayList.size(); i++) {
            String s=expressionAsArrayList.get(i);
            if (s.equals("("))
                operatorsStack.add('(');
            else if (s.equals(")")) {
                while (operatorsStack.getLast() != '(')
                    processOperator(operandsStack, operatorsStack.removeLast());
                operatorsStack.removeLast();
            } else if (s.length()==1 && isOperator(s.charAt(0))) {
                while (!operatorsStack.isEmpty() && priority(operatorsStack.getLast()) >= priority(s.charAt(0)))
                    processOperator(operandsStack, operatorsStack.removeLast());
                operatorsStack.add(s.charAt(0));
            } else {
                String operand=s;

                // check is current number correct or not:
                Pattern pattern
                        =Pattern.compile("^(\\s?\\-?(0|[1-9])\\d*\\s?)|(\\s?\\-?[1-9][0-9]*\\.\\d+\\s?)|(\\s?\\-?0\\.\\d+\\s?)");
                        //=Pattern.compile("[-]?[0-9]+(.[0-9]+)?");
                        //=Pattern.compile("^.*"); // turn off numbers validity check

                Matcher matcher=pattern.matcher(operand);

                if(!matcher.matches()) {
                    System.out.println("Wrong format of number detected: "+operand);
                    errorDetected=true;
                    break;
                }
                operandsStack.add(operand);
            }
        }

        if(errorDetected) {
            return "0";
        }

        while (!operatorsStack.isEmpty()) {
            processOperator(operandsStack, operatorsStack.removeLast());
        }
        if(operandsStack.size()>1 && !operatorsStack.isEmpty()) {
            System.out.print("Error in the expression");
            errorDetected=true;
            return "0";
        }
        return operandsStack.get(0);
    }


    public static void main(String[] args) {
        System.out.println("For exit input: exit");
        System.out.println("App supports brackets and mathematical operations: + , - , * , / , ^");
        System.out.println("Input your mathematical expression below");
        result="0";

        //expression="00.2+2"; // - inner input

        scanner=new Scanner(System.in);
        while (scanner.hasNextLine()) {
            expression=scanner.nextLine();


            if(expression.equals("exit")) {
                scanner.close();
                return;
            }

            inputBasicCheck(expression);

            if(errorDetected) {
                System.err.println("Input error: wrong input");
            } else {
                expression=modifyExpression(expression);
                result=evaluate(expressionAsArrayList);

                if(errorDetected) {
                    System.err.println("Input error: wrong input");
                } else {
                    System.out.println("Result: "+result);
                }
            }
        }
        scanner.close();
    }
}





