import javax.script.ScriptException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pazuk on 12.02.2018.
 */

public class DIYcalculator {


    static boolean errorDetected = false;

    static Scanner scanner;



    static boolean isDelimeter(char c) {
        return c == ' ';
    }


    static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }


    static int priority(char operator) {
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


    static void processOperator(LinkedList<Double> numbersStack, char operator) {
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


    public static Double evaluate(String exp) { // calculate with railway shunting yard method & reverse Polish notation


        LinkedList<Double> operandsStack = new LinkedList<>();
        LinkedList<Character> operatorsStack = new LinkedList<>();
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (isDelimeter(c)) {
                continue;
            }

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
                    if(Character.isDigit(exp.charAt(i)) || exp.charAt(i)=='.'){
                        operand+=exp.charAt(i);
                    } else {
                        break;
                    }
                    i++;
                }
                i--;
                Pattern pattern=Pattern.compile("^(\\d+)|([1-9][0-9]*\\.\\d+)|(0\\.\\d+)|(0,\\d+)");
                Matcher matcher=pattern.matcher(operand);

                if(!matcher.matches()) {
                    errorDetected=true;
                    break;
                } else {
                    int g=0;
                    while (g<operand.length()) {
                        if(operand.charAt(g)==',') {
                            operand=operand.replace(',', '.');

                            break;
                        }
                        g++;
                    }
                    //System.out.println(operand);
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

        //System.out.println(operandsStack);
        return operandsStack.get(0);
    }


    public static void main(String[] args) {

        String expression;
        //expression = "11,6";

        scanner=new Scanner(System.in);
        expression=scanner.next();
        scanner.close();






        if(expression.length()>0) {

            Pattern pattern=Pattern.compile("^[0-9\\s\\(\\)\\*\\+\\.,^-]");
            Matcher matcher;

            String[] array=expression.split("");

            int i=0;
            while (i<array.length) {
                matcher=pattern.matcher(array[i]);
                if(!matcher.matches()) {
                    errorDetected=true;
                    break;
                }
                i++;
            }

            if(errorDetected) {
                System.out.println("Wrong expression input");
            } else {
                Double result=evaluate(expression);

                if(errorDetected) {
                    System.out.println("Wrong number input");
                } else {
                    System.out.println("Result: "+result);
                    if(result.isInfinite()) {
                        System.out.println("Division by zero");
                    } else if(result.isNaN()) {
                        System.out.println("Division of zero by zero or any other case which led to Not-a-Number result");
                    }
                }
            }

        } else {
            System.out.println("Empty expression");
        }



    }

}





