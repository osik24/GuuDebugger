package sample;

import java.io.FileReader;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class Controller {

    private int curIndexString;
    private ArrayList<String> curString;

    private ArrayList<Integer> indexFunctionList = new ArrayList<>();
    private ArrayList<String> functionList = new ArrayList<>();

    private LinkedList<Integer> stackTrace = new LinkedList<>();
    private LinkedList<Integer> stackTraceIndex = new LinkedList<>();
    private HashMap<String, Integer> variables = new HashMap<>();

    @FXML
    private Button buttonVar;

    @FXML
    private Button buttonStepOver;

    @FXML
    private Button buttonStepInto;

    @FXML
    private Button buttonTrace;

    @FXML
    private ListView<String> codeList;

    @FXML
    void initialize() throws Exception {
        FileReader fr = new FileReader("main.guu");
        Scanner frScan = new Scanner(fr);


        for(int i = 0; frScan.hasNextLine(); i++) {
            codeList.getItems().add("(" + (i + 1) + ") " + frScan.nextLine());
        }

        buttonTrace.setOnAction(actionEvent -> printStackTrace());
        buttonVar.setOnAction(actionEvent -> printVariables());
        buttonStepOver.setOnAction(actionEvent -> stepOver());
        buttonStepInto.setOnAction(actionEvent -> stepInto());

        createFunctionList();
        goToFunction(findFuncPosition("main"));

        fr.close();
    }

    private void printVariables() {
        if(variables.size() == 0) {
            System.out.println("0 variable declared");
        } else {
            System.out.println("Variables:");
            for(Map.Entry<String, Integer> entry : variables.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
        System.out.println();
    }

    private void printStackTrace() {
        ListIterator traceIterator = stackTrace.listIterator(stackTrace.size());
        ListIterator traceIndexIterator = stackTraceIndex.listIterator(stackTraceIndex.size());

        System.out.println("Stack trace:");
        if(traceIterator.hasPrevious()) {
            System.out.println(findFuncName((int)traceIterator.previous()));
        }
        while(traceIterator.hasPrevious()) {
            System.out.println(findFuncName((int)traceIterator.previous()) + " (" + ((int)traceIndexIterator.previous() + 1) + ")");
        }
        System.out.println();
    }

    private void stepOver() {
        if(!curString.get(0).equals("call")) {
            doCommand();
        }
        goToNextCommand();
    }

    private void stepInto() {
        if(!curString.get(0).equals("call")) {
            doCommand();
            goToNextCommand();
        } else {
            stackTraceIndex.add(curIndexString);
            goToFunction(findFuncPosition(curString.get(1)));
        }
    }

    private void doCommand() {
        if(curString.get(0).equals("set")) {
            variables.put(curString.get(1), Integer.parseInt(curString.get(2)));
        } else if(curString.get(0).equals("print")) {
            System.out.println(curString.get(1) + " = " + variables.get(curString.get(1)));
        }
    }

    private String findFuncName(int idx) {
        for(int i = 0; i < indexFunctionList.size(); i++) {
            if(indexFunctionList.get(i) == idx) {
                return functionList.get(i);
            }
        }
        System.out.println("Incorrect func index");
        return "";
    }

    private int findFuncPosition(String func) {
        for(int i = 0; i < functionList.size(); i++) {
            if(functionList.get(i).equals(func)) {
                return indexFunctionList.get(i);
            }
        }
        System.out.println("Incorrect func name");
        return -1;
    }

    private void goToFunction(int i) {
        if(i == -1) {
            System.out.println("Main not found");
            return;
        }
        setNewCurIndexString(i);
        stackTrace.add(i);
        goToNextCommand();
    }

    private void goToNextCommand() {
        int i = curIndexString + 1;
        while(i < codeList.getItems().size()) {
            ArrayList<String> al = mySplit(codeList.getItems().get(i));
            if(al.size() > 0) {
                if(al.get(0).equals("sub")) {
                    break;
                } else {
                    setNewCurIndexString(i);
                    return;
                }
            }
            i++;
        }

        stackTraceBack();
    }

    private void stackTraceBack() {
        stackTrace.removeLast();
        if(stackTrace.size() == 0) {
            buttonStepInto.setDisable(true);
            buttonStepOver.setDisable(true);
            buttonTrace.setDisable(true);
            buttonVar.setDisable(true);
            String curString = codeList.getItems().get(curIndexString);
            if(curString.charAt(0) == '>') {
                curString = curString.substring(1);
                codeList.getItems().set(curIndexString, curString);
            }
        } else {
            setNewCurIndexString(stackTraceIndex.getLast());
            stackTraceIndex.removeLast();
            goToNextCommand();
        }
    }

    private void setNewCurIndexString(int i) {
        String curString = codeList.getItems().get(curIndexString);
        if(curString.charAt(0) == '>') {
            curString = curString.substring(1);
            codeList.getItems().set(curIndexString, curString);
        }
        curIndexString = i;
        codeList.getItems().set(curIndexString, ">" + codeList.getItems().get(curIndexString));
        updateCurString();
    }

    private void createFunctionList() {
        for(int i = 0; i < codeList.getItems().size(); i++) {
            ArrayList<String> al = mySplit(codeList.getItems().get(i));
            if(al.size() > 0 && al.get(0).equals("sub")) {
                functionList.add(al.get(1));
                indexFunctionList.add(i);
            }
        }
    }

    private void updateCurString() {
        curString = mySplit(codeList.getItems().get(curIndexString));
    }

    private ArrayList<String> mySplit(String soursStr) {
        String[] tmpStr = soursStr.split(" ");
        ArrayList<String> res = new ArrayList<>();
        for(String str : tmpStr) {
            if(checkString(str)) {
                res.add(str);
            }
        }
        return res;
    }

    private boolean checkString(String str) {
        try {
            return str.charAt(0) >= 'a' && str.charAt(0) <= 'z' || str.charAt(0) >= 'A' && str.charAt(0) <= 'Z'
                    || str.charAt(0) >= '0' && str.charAt(0) <= '9' || str.charAt(0) == '_';
        } catch(Exception ex) {
            return false;
        }

    }
}
