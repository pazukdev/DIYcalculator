import javax.script.ScriptException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pazuk on 12.02.2018.
 */

public class DIYcalculator {

    private static String expression;

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
                System.out.println("Wrong expression input. Not allowed characters are detected");
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
            System.out.println("Error in the expression: check if the brackets are positioned correctly");
            errorDetected=true;
            return;
        }

        i=0;
        while (i<expression.length()) {
            if(expression.charAt(i)=='(' || expression.charAt(i)==')') { // check if the first bracket in...
                if(expression.charAt(i)==')') { // ...extension is not close bracket
                    System.out.println("Error in the expression: check if the brackets are positioned correctly");
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
        expression=expression.replace("(-", "(0-");
        if(expression.charAt(0)=='-') {
            expression="0"+expression;
        }
        return expression;
    }



    private static boolean isDelimeter(char c) {
        return c == ' ';
    }


    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
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


    private static Double evaluate(String exp) { // calculate with railway shunting yard method & reverse Polish notation


        LinkedList<Double> operandsStack = new LinkedList<>();
        LinkedList<Character> operatorsStack = new LinkedList<>();
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            //if (isDelimeter(c)) {
            //   continue;
            //}

            if (c == '(')
                operatorsStack.add('(');
            else if (c == ')') {
                while (operatorsStack.getLast() != '(')
                    processOperator(operandsStack, operatorsStack.removeLast());
                operatorsStack.removeLast();
            } else if (isOperator(c)) {
                while (!operatorsStack.isEmpty() && priority(operatorsStack.getLast()) >= priority(c))
                    processOperator(operandsStack, operatorsStack.removeLast());
                operatorsStack.add(c);
            } else {
                String operand = "";
                int count=0;

                while (i < exp.length()) {
                    if(Character.isDigit(exp.charAt(i)) || exp.charAt(i)=='.' || exp.charAt(i)==' '){
                        operand+=exp.charAt(i);
                    } else {
                        break;
                    }
                    i++;
                }
                i--;
                // check is current number correct or not:
                Pattern pattern
                        =Pattern.compile("^(\\s)|(\\s?(0|[1-9])\\d*\\s?)|(\\s?[1-9][0-9]*\\.\\d+\\s?)|(\\s?0\\.\\d+\\s?)");

                Matcher matcher=pattern.matcher(operand);

                if(!matcher.matches()) {
                    System.out.println("Wrong format of number detected");
                    errorDetected=true;
                    break;
                }

                if(!operand.equals(" ")) {
                    if(operand.charAt(0)==' ') {
                        operand=operand.substring(1);
                    }

                    if(operand.charAt(operand.length()-1)==' ') {
                        operand=operand.substring(0, operand.length()-1);
                    }

                    operandsStack.add(Double.parseDouble(operand));
                }

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

        //expression = "(17 ^ 4 + 5 * 974 ^ 33 + 2.24 * 4.75)^0";

        scanner=new Scanner(System.in);
        expression=scanner.next();
        scanner.close();

        inputBasicCheck(expression);

        if(errorDetected) {
            System.err.println("Input error: wrong input");
        } else {
            expression=modifyExpression(expression);
            System.out.println(expression+" =");
            Double result=evaluate(expression);

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





