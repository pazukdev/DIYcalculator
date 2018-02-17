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

    private static boolean errorDetected;
    private static Scanner scanner;

    private static void inputBasicCheck(String expression) { // at first - check inputted expression for
                                                             // some elementary syntax errors

        Pattern patternValidCharacters=Pattern.compile("^[0-9\\s\\(\\)\\*\\+\\.,^/-]");
        Matcher matcher;

        String[] expressionArray=expression.split("");

        if(expression.isEmpty()) {
            System.out.println("Nothing has been entered");
            errorDetected=true;
            return;
        }

        int i=0;
        while (i<expressionArray.length) {
            matcher=patternValidCharacters.matcher(expressionArray[i]);
            if(!matcher.matches()) {
                System.out.println("Wrong expression input. Not allowed character detected: "+expressionArray[i]);
                errorDetected=true;
                return;
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


    private static void modifyExpression(String expression) { // change inputed expression to the form convenient for
                                                              // further processing and evaluating with shunting yard
                                                              // method, postfix notation and postfix evaluation
                                                              // algorithm
        expression=expression.replace(',', '.');

        expressionAsArrayList =new ArrayList<>();

        int u=0;
        for (int i = 0; i < expression.length(); i++) {
            String s=Character.toString(expression.charAt(i));
            if (isBracket(s)) {
                expressionAsArrayList.add(u, s);
                u++;
            }

            else if (isOperator(s)) {
                expressionAsArrayList.add(u, s);
                u++;
            } else {
                String operand = "";
                while (i < expression.length() && !isBracket(Character.toString(expression.charAt(i)))
                        && !isOperator(Character.toString(expression.charAt(i)))) {
                    operand=operand+expression.charAt(i);
                    i++;
                }
                i--;
                expressionAsArrayList.add(u, operand);
                u++;
            }
        }

        //System.out.println(expressionAsArrayList); // - show parsed string intermediate stage

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

        if(expressionAsArrayList.size()<3) {
            System.out.println("This is incomplete mathematical expression");
            errorDetected=true;
            return;
        }

        String s0=expressionAsArrayList.get(0);
        String s1=expressionAsArrayList.get(1);
        String s2=expressionAsArrayList.get(2);

        if(isOperator(s0)) {
            if(!s0.equals("-")) {
                errorDetected=true;
                System.out.println("Wrong start of the expression: "+s0
                        +" Expression can't start with + , * , / or ^");
                return;
            } else if(!s1.equals("(") || !digitFinder(s1)) {
                errorDetected=true;
                System.out.println("Wrong expression. Expression can't have such a sequence of operators:"+s0+s1 );
                return;
            } else if(digitFinder(s1)) {
                expressionAsArrayList.set(1, "-"+s1);
                expressionAsArrayList.remove(0);
            }
        }

        for(int i=2; i<expressionAsArrayList.size(); i++) {
            s0=expressionAsArrayList.get(i-2);
            s1=expressionAsArrayList.get(i-1);
            s2=expressionAsArrayList.get(i);

            if(isOperator(s2) && isOperator(s1) && isOperator(s0)) {
                errorDetected=true;
                System.out.println("Wrong expression. Expression can't have such a sequence of operators:"+s0+s1+s2);
                return;
            } else if (s1.equals("(") && s0.equals(")")) {
                errorDetected=true;
                System.out.println("Wrong expression. Expression can't have such a sequence of brackets"+s0+s1);
                return;
            } else if(s1.equals("+") && !digitFinder(s0) && !s0.equals(")")) {
                s1=s1+s2;
                errorDetected=true;
                System.out.println("Wrong number format detected: "+s1+" Input X instead of +X");
                return;
            } else if(s1.equals("^") && isOperator(s0)) {
                errorDetected=true;
                System.out.println("Wrong expression. Expression can't have such a sequence of operators:"+s0+s1 );
                return;
            } else if(s1.equals("^") && !s2.equals("(") && !s2.equals("-") && !digitFinder(s2)) {
                errorDetected=true;
                System.out.println("Wrong expression. Expression can't have such a sequence of operators:"+s0+s1 );
                return;
            } else if(s1.equals("-") && digitFinder(s2) && (isOperator(s0) || s0.equals("("))) {
                s1=s1+s2;
                expressionAsArrayList.set(i-1, s1);
                expressionAsArrayList.remove(i);
            }
        }

        int operandCounter=0;
        int operatorCounter=0;
        int openBracketCounter=0;
        int closeBracketCounter=0;

        for(int i=0; i<expressionAsArrayList.size(); i++) {
            String s=expressionAsArrayList.get(i);
            if(isOperator(s)) {
                operatorCounter++;
            } else if (s.equals("(")) {
                openBracketCounter++;
            } else if (s.equals(")")) {
                closeBracketCounter++;
            } else {
                operandCounter++;
            }
        }

        if(operatorCounter!=operandCounter-1) {
            System.out.println("This is incomplete mathematical expression: number of operators doesn't match" +
                    "number of operands");
        }

        if(openBracketCounter!=closeBracketCounter) {
            System.out.println("Error in the expression: wrong number of brackets " +
                    "\nCheck if the brackets are positioned correctly");
            errorDetected=true;
            return;
        }

        int i=0;
        while (i<expressionAsArrayList.size()) {
            String s=expressionAsArrayList.get(i);
            if(s.equals("(") || s.equals(")")) { // check if the first bracket in...
                if(s.equals(")")) {              // ...extension is not close bracket
                    System.out.println("Error in the expression: first bracket shouldn't be closing " +
                            "\ncheck if the brackets are positioned correctly");
                    errorDetected=true;
                    return;
                }
                break;

            }
            i++;
        }

    }

    private static boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("^");
    }

    private static boolean isBracket(String s) {
        return s.equals("(") || s.equals(")");
    }


    private static int priority(String operator) {
        switch (operator) {
            case "+":
                return 1;
            case "-":
                return 1;

            case "*":
                return 2;
            case "/":
                return 2;

            case "^":
                return 3;

            default:
                return -1;
        }
    }


    private static void processOperator(LinkedList<String> numbersStack, String operator) {
        BigDecimal topNumber=new BigDecimal(numbersStack.removeLast());
        BigDecimal nextTopNumber=new BigDecimal(numbersStack.removeLast());
        switch (operator) {
            case "+":
                numbersStack.add(String.valueOf(nextTopNumber.add(topNumber)));
                break;
            case "-":
                numbersStack.add(String.valueOf(nextTopNumber.subtract(topNumber)));
                break;
            case "*":
                numbersStack.add(String.valueOf(nextTopNumber.multiply(topNumber)));
                break;
            case "/":
                double d=topNumber.doubleValue();
                if(d==0) {
                    System.out.println("Dividing by zero impossible");
                    errorDetected=true;
                    break;
                }
                numbersStack.add(String.valueOf(nextTopNumber.divide(topNumber, 16, BigDecimal.ROUND_HALF_UP)));
                break;
            case "^":
                double d1=topNumber.doubleValue();
                double d2=nextTopNumber.doubleValue();
                numbersStack.add(String.valueOf(Math.pow(d2, d1)));
                break;
        }
    }


    private static String evaluate(ArrayList<String> expressionAsArrayList) { // calculate with railway shunting yard method
                                                                              // & reverse Polish notation

        System.out.println("Parsed expression: "+expressionAsArrayList); // parsed input final stage

        LinkedList<String> operandsStack = new LinkedList<>();
        LinkedList<String> operatorsStack = new LinkedList<>();
        for (int i = 0; i < expressionAsArrayList.size(); i++) {
            String s=expressionAsArrayList.get(i);
            if (s.equals("("))
                operatorsStack.add(s);
            else if (s.equals(")")) {
                while (!operatorsStack.getLast().equals("("))
                    processOperator(operandsStack, operatorsStack.removeLast());
                operatorsStack.removeLast();
            } else if (isOperator(s) && s.length()==1) {
                while (!operatorsStack.isEmpty() && priority(operatorsStack.getLast()) >= priority(s)) {
                    processOperator(operandsStack, operatorsStack.removeLast());
                }
                operatorsStack.add(s);
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
                    return "-";
                }
                operandsStack.add(operand);
            }
        }

        while (!operatorsStack.isEmpty() && !errorDetected) {
            processOperator(operandsStack, operatorsStack.removeLast());
        }
        if(operandsStack.size()>1 && !operatorsStack.isEmpty()) {
            System.out.print("Error in the expression");
            errorDetected=true;
            return "0";
        }
        if(errorDetected) {
            return "0";
        } else {
            return operandsStack.get(0);
        }
    }


    public static void main(String[] args) {
        System.out.println("For exit input: exit");
        System.out.println("App supports brackets and mathematical operations: + , - , * , / , ^");
        System.out.println("Input your mathematical expression below");
        result="-";

        //expression="00.2+2"; // - inner input

        scanner=new Scanner(System.in);
        while (scanner.hasNextLine()) {
            errorDetected=false;
            expression=scanner.nextLine();

            if(expression.equals("exit")) {
                scanner.close();
                return;
            }
            inputBasicCheck(expression);
            if(!errorDetected) {
                modifyExpression(expression);
            }
            if(!errorDetected) {
                result=evaluate(expressionAsArrayList);
            }
            if(!errorDetected) {
                System.out.println("Result: "+result);
            }
            if(errorDetected) {
                System.err.println("Input error: wrong input");
            }
        }
    }
}





