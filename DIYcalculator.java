import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pazuk on 12.02.2018.
 */

    //Plan:
    // #1. Check stage#1: check inputed expression for some elementary errors
    // #2. Parse expression
    // #3. Check stage#2: check parsed expression
    // #4. Bring expression to postfix notation form with shunting-yard algorithm and evaluate it

public class DIYcalculator {

    private static String expression;
    private static ArrayList<String> expressionAsArrayList;
    private  static  String result;

    private static boolean errorDetected; // if any error detected at any stage of processing of inputed
                                          // expression - stop processing and output error
    private static Scanner scanner;

    private static void inputBasicCheck(String expression) { // #1. Check stage#1: check inputed expression for some
                                                             // elementary errors

        Pattern patternValidCharacters=Pattern.compile("^[0-9\\s\\(\\)\\*\\+\\.,^/-]");
        Matcher matcher;

        if(expression.isEmpty()) { // check for empty input
            System.out.println("Nothing has been entered");
            errorDetected=true;
            return;
        }

        if(expression.length()<3) { // check if inputed expression is not too short for be an expression
            System.out.println("This is incomplete mathematical expression");
            errorDetected=true;
        }

        String[] expressionArray=expression.split("");
        int i=0;
        while (i<expressionArray.length) {
            matcher=patternValidCharacters.matcher(expressionArray[i]);
            if(!matcher.matches()) { // check for not allowed characters in expression
                System.out.println("Wrong expression input. Not allowed character detected: "+expressionArray[i]);
                errorDetected=true;
                return;
            }

            i++;
        }

    }

    private static void parseExpression(String expression) { // #2. Parse expression
        expression=expression.replace(',', '.'); // according with task input of 0,1 should be same valid as 0.1

        expressionAsArrayList =new ArrayList<>();
        // parsing:
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
            if(digitFinder(s)) { // if element in array is number - remove spaces before and after number. Not in the
                                 // number
                if(s.charAt(0)==' ') {
                    s=s.substring(1);
                }
                if(s.charAt(s.length()-1)==' ') {
                    s=s.substring(0, s.length()-1);
                }
                expressionAsArrayList.set(i, s);
            } else { // if element in array is operator or bracket or element with spaces only -remove all the spaces
                s=s.replace(" ", "");
                expressionAsArrayList.set(i, s);
            }
        }

        for(int i=0; i<expressionAsArrayList.size(); i++) {
            String s = expressionAsArrayList.get(i);
            if (s.equals("")) { // remove all empty elements
                expressionAsArrayList.remove(i);
            }
        }

        // #3. Check stage#2: check parsed expression:

        if(expressionAsArrayList.size()<3) { // this expression theoretically is too short to be expression
            System.out.println("This is incomplete mathematical expression");
            errorDetected=true;
            return;
        }

        String s0=expressionAsArrayList.get(0);
        String s1=expressionAsArrayList.get(1);
        String s2=expressionAsArrayList.get(2);

        if(isOperator(s0)) { // check begin of expression
            if(!s0.equals("-")) {
                errorDetected=true;
                System.out.println("Wrong start of the expression: "+s0
                        +" Expression can't start with + , * , / or ^");
                return;
            } else if(digitFinder(s1)) {
                expressionAsArrayList.set(1, "-"+s1);
                expressionAsArrayList.remove(0);
            } else {
                errorDetected = true;
                System.out.println("Wrong expression. Expression can't start with such a sequence of operators:" + s0 + s1);
                return;
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
            } else if(s1.equals("-") && digitFinder(s2) && (isOperator(s0) || s0.equals("("))) { // detecting elements
                          // where "-" is not operator in binary expression, but is part of negative number
                s1=s1+s2; // and uniting them: before: [ -, 1], after: [-1]
                expressionAsArrayList.set(i-1, s1);
                expressionAsArrayList.remove(i);
            }
        }

        // count how much different elements in expression...

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

        //...and draw some conclusions from result of counting:

        if(operatorCounter>operandCounter) {
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


    private static String evaluate(ArrayList<String> expressionAsArrayList) { // #4. Bring expression to postfix
                                                                              // notation form with shunting-yard
                                                                              // algorithm and evaluate it

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
                String operand=s; // if element is not operator or bracket - so it is operand

                // check is current number correct or not:
                Pattern patternMainCheck
                        =Pattern.compile("^(\\-?(0|[1-9]\\d*))|(\\-?[1-9][0-9]*\\.\\d+)|(\\-?0\\.\\d+)");// manual check
                        //=Pattern.compile("^.*"); // turn off numbers validity manual check
                Matcher matcher=patternMainCheck.matcher(operand);
                if(!matcher.matches()) {
                    System.out.println("Wrong format of number detected: "+operand);
                    errorDetected=true;
                    return "0";
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

    // auxiliary methods begin

    private static boolean digitFinder(String s) {
        String[] stringList=s.split("");
        for(int i=0; i<s.length(); i++) {
            if(stringList[i].matches("[0-9]")) {
                return true;
            }
        }
        return false;
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
                if(d==0) { // dividing by zero manual check
                    System.out.println("Dividing by zero impossible: "+nextTopNumber+"/"+0);
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

    // auxiliary methods end


    public static void main(String[] args) throws Exception {
        System.out.println("For exit input: exit");
        System.out.println("App supports brackets and mathematical operations: + , - , * , / , ^");
        System.out.println("Input your mathematical expression below");
        result="-";

        //expression="(17 ^ 4 + 5 * 974 ^ 33 + 2.24 * 4.75)^0"; // - inner input instead of console

        scanner=new Scanner(System.in);
        while (scanner.hasNextLine()) {
            errorDetected=false;
            expression=scanner.nextLine();

            if(expression.equals("exit") || expression.equals("EXIT") || expression.equals("Exit")) {
                scanner.close();
                return;
            }
            inputBasicCheck(expression);
            if(!errorDetected) {
                parseExpression(expression);
            }
            if(!errorDetected) {
                try {
                    result=evaluate(expressionAsArrayList);
                } catch (NumberFormatException e) {
                    errorDetected=true;
                    System.out.println(e);
                } catch (ArithmeticException e) {
                    errorDetected=true;
                    System.out.println(e);
                } catch (Exception e) { // catch exceptions what was not prevented by expression check stage#1,
                                        // check stage#2 and manual check for correct numbers
                    errorDetected=true;
                    System.out.println(e);
                }

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





