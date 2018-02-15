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
    private  static  Double result;

    private static boolean errorDetected = false;
    private static Scanner scanner;

    private static void inputBasicCheck(String expression) { // at first - check inputted expression for
                                                             // some elementary syntax errors

        Pattern pattern=Pattern.compile("^[0-9\\s\\(\\)\\*\\+\\.,^/-]");
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
            matcher=pattern.matcher(expressionArray[i]);
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


        if(expressionAsArrayList.get(0).equals("-")) {
            expressionAsArrayList.set(1, "-"+expressionAsArrayList.get(1));
            expressionAsArrayList.remove(0);
        }

        for(int i=0; i<expressionAsArrayList.size(); i++) {
            String s=expressionAsArrayList.get(i);
            if(s.equals(" ")) {
                expressionAsArrayList.remove(i);
            }
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


    private static void processOperator(LinkedList<Double> numbersStack, char operator) {
        Double topNumber = numbersStack.removeLast();
        Double nextTopNumber = numbersStack.removeLast();
        switch (operator) {
            case '+':
                numbersStack.add(nextTopNumber + topNumber);
                break;
            case '-':
                numbersStack.add(nextTopNumber - topNumber);
                break;
            case '*':
                numbersStack.add(nextTopNumber * topNumber);
                break;
            case '/':
                numbersStack.add(nextTopNumber / topNumber);
                break;
            case '^':
                numbersStack.add(Math.pow(nextTopNumber, topNumber));
                break;
        }
    }


    private static Double evaluate(ArrayList<String> expression) { // calculate with railway shunting yard method
                                                                   // & reverse Polish notation

        System.out.println("Parsed expression: "+expressionAsArrayList); // parsed input final stage

        LinkedList<Double> operandsStack = new LinkedList<>();
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
                        //=Pattern.compile("^.*"); // turn off numbers validity check

                Matcher matcher=pattern.matcher(operand);

                if(!matcher.matches()) {
                    System.out.println("Wrong format of number detected: "+operand);
                    errorDetected=true;
                    break;
                }
                operandsStack.add(Double.parseDouble(operand));
            }
        }

        if(errorDetected) {

            return 0.0;
        }

        while (!operatorsStack.isEmpty()) {
            processOperator(operandsStack, operatorsStack.removeLast());
        }
        if(operandsStack.size()>1 && !operatorsStack.isEmpty()) {
            System.out.print("Error in the expression");
            errorDetected=true;
            return 0.0;
        }
        return operandsStack.get(0);
    }


    public static void main(String[] args) {
        result=0.0;

        //expression="(4 + 3) * 2 ^ -2"; // - inner input

        scanner=new Scanner(System.in);
        expression=scanner.nextLine();
        scanner.close();

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
                if(result.isInfinite()) {
                    System.err.println("Division by zero");
                } else if(result.isNaN()) {
                    System.err.println("Division of zero by zero or any other case which led to Not-a-Number result");
                }
            }
        }

    }

}





