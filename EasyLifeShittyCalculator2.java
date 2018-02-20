import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

/**
 * Created by Pazuk on 19.02.2018.
 */
public class EasyLifeShittyCalculator2 {

    public static void main(String[] args) throws ScriptException {
        System.out.println("For exit print: stop this, please!! stop!! aaa!!!");

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String expression = scanner.nextLine();
            if (expression.equals("stop this, please!! stop!! aaa!!!")) {
                break;
            }
            try {
                System.out.println("= "+engine.eval(expression));
            } catch (Exception e) {
                System.out.println(e);
            }

        }
        scanner.close();


    }

}

