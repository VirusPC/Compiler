package syntax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Parser {
    public static String OVER = "#"; //结束符
    public static String EMPTY = "ε"; //空
    public static String LINK = "->"; //文法连接符
    public static String SEPARATOR = "|"; //文法分隔符
    public static String WORD_SEPARATOR = "_"; //单词分隔符
    public static String POINT = "·"; //项目的点
    public static String EXTEND_START_SYMBOL = "extend"; //默认拓展开始符
    public static int INITIAL_STATE = 0; //初始状态

    private List<List<String>> grammars; // 文法
    private String startSymbol;  // 开始符
    private Set<String> vnSet; // 非终结符集合
    private Set<String> vtSet; // 终结符集合
    private Map<String, Integer> toEmpty; //非终结符是否可以推出空串 1是 0未定 -1否

    private List<List<int[]>> itemLists; // 项目集 Integer[0]为grammar序号， Integer[1]为指针
    private List<Map<String, Integer>> goTable; // 项目集的go()
    private boolean[] passedGrammar; // 标记走过的路径

    private List<Map<String, String>> actionTable; // action表
    private List<Map<String, Integer>> gotoTable; // goto表

    /**
     * 读取文件，根据文件内容生成vn、vt 要求：符合LR(0)文法规则
     * @param path 文件路径
     * @param startSymbol 起始符
     */
    public Parser(String path, String startSymbol) {
        createGrammars(path, startSymbol);
        fillToEmpty();
//            init(path, startSymbol);
//            extend();
//            createItemLists();
//            createActionAndGoto();
    }

//    public Parser(String path) {
//        this(path, DEFALT_START_SYMBOL);
//    }
    
    /**
     * 初始化extendStartSymbol、vn、vt、grammars
     * 
     * @param path
     * @param startSymbol
     */
    private void createGrammars(String path, String startSymbol) {
        this.startSymbol = startSymbol;
        grammars = new ArrayList();
        vnSet = new HashSet<String>();
        vtSet = new HashSet<String>();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);
            String line;
            /*
             * 按行读取文法，文法格式为symbol[0]->symbol[1], symbol[0]为终结符
             * symbol[1]为终结符与非终结符组成的表达式
             */
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if ("".equals(line)) {
                    continue;
                }
                String symbol[] = line.split(LINK);
                if (symbol.length != 2) {
                    continue;
                }
                symbol[0] = symbol[0].trim();
                symbol[1] = symbol[1].trim();
                String[] rights = symbol[1].split("\\"+SEPARATOR);

                // 将->左边的符号加入到非终结符集合中
                vnSet.add(symbol[0]);

                for(String right : rights){
                    List<String> grammar = new ArrayList<String>();
                    List<String> words = Arrays.asList(right.split(WORD_SEPARATOR));
                    grammar.add(symbol[0]);
                    grammar.addAll(words);
                    vtSet.addAll(words);
                    grammars.add(grammar);
                }

            }
            // 将非终结符从终结符集合中剔除
            for (String word : vnSet) {
                if (vtSet.contains(word)) {
                    vtSet.remove(word);
                }
            }
            if (vtSet.contains(EMPTY)) {
                vtSet.remove(EMPTY);
            }
            if (vtSet.contains(SEPARATOR)) {
                vtSet.remove(SEPARATOR);
            }
            extend();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found!");
        } catch (IOException e) {
            System.out.println("IO Error!");
        } catch (Exception e) {
            System.out.println("Start-Symbol Error!");
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 扩展文法
     */
    private void extend() {
        List<String> grammar = new ArrayList<String>();
        grammar.add(EXTEND_START_SYMBOL);
        grammar.add(startSymbol);
        grammars.add(0, grammar);
        vnSet.add(EXTEND_START_SYMBOL);
    }


    private void fillToEmpty(){
        //创建toEmpt映射表 0未定 1是 -1否
        toEmpty = new HashMap<String, Integer>();
        for(String word : vnSet){
            toEmpty.put(word, 0);
        }
        //记录文法是否被删除
        boolean[] deleted = new boolean[grammars.size()];

        //List<List<String>> grammarsForVn = new ArrayList<List<String>>();
        for(String word : vnSet){
            for(int i=0; i<grammars.size(); i++){
                List<String> grammar = grammars.get(i);
                if(grammars.get(0).equals(word)){
                    //grammarsForVn.add(grammar);
                }
            }
//            for(List<String> grammar : grammarsForVn){
//                if(EMPTY.equals(grammar.get(1))){
//                    toEmpty.put(grammar.get(0), 1);
//                    deleted[i] = true;
//                }
//            }
        }

        for(int i=0; i<grammars.size(); i++) {
            List<String> grammar = grammars.get(i);
            if(EMPTY.equals(grammar.get(1))){
                toEmpty.put(grammar.get(0), 1);
                deleted[i] = true;
            } else {
                for (int j=1; j<grammar.size(); j++) {
                    if (vtSet.contains(grammar.get(j))) {
                        toEmpty.put(grammar.get(0), -1);
                        deleted[i] = true;
                        break;
                    }
                }
            }
        }
        int i = 1;
    }

//    /**
//     * 创建项目集
//     */
//    private void createItemLists() {
//        itemLists = new ArrayList<List<int[]>>();
//        goTable = new ArrayList<Map<String, Integer>>();
//        itemLists.add(firstItemList());
//        for (int i = 0; i < itemLists.size(); i++) {//
//            List<int[]> itemList = itemLists.get(i);
//            Map<String, Integer> go = new HashMap<String, Integer>();
//            int ItemListCount = itemLists.size();
//
//            //创建项目集
//            for (int[] item : itemList) {
//                String[] grammar = grammars.get(item[0]);
//                if (grammar[1].length() <= item[1]) {
//                    continue;
//                }
//                String symbol = grammar[1].substring(item[1], item[1]+1);
//                int[] newItem = { item[0], item[1]+1};
//                passedGrammar = new boolean[grammars.size()];
//                if (!go.containsKey(symbol)) {
//                    go.put(symbol, itemLists.size());
//                    itemLists.add(closure(newItem));
//                } else {
//                    int nextItemListNum = go.get(symbol);
//                    itemLists.get(nextItemListNum).addAll(closure(newItem));
//                }
//
//            }
//
//            //将新建的项目集与之前的项目集比较，判断是否相同
//            for (int after = ItemListCount; after < itemLists.size(); after++) {
//                List<int[]> newItemList = itemLists.get(after);
//                for (int before = 0; before < ItemListCount; before++) {
//                    List<int[]> oldItemList = itemLists.get(before);
//                    if (compareItemList(newItemList, oldItemList)) {
//                        for (String key : go.keySet()) {
//                            Integer now = go.get(key);
//                            if (now.equals(after)) {
//                                go.put(key, before);
//                            } else if (now > after) {
//                                go.put(key, now - 1);
//                            }
//                        }
//                        itemLists.remove(itemLists.get(after));
//                        after--;
//                    }
//                }
//            }
//
//            goTable.add(go);
//        } // 遍历itemList结束
//
//    }
//
//    /**
//     * @return 第一个状态的项目集
//     */
//    private List<int[]> firstItemList() {
//        passedGrammar = new boolean[grammars.size()];
//        for (int i = 0; i < grammars.size(); i++) {
//            String[] grammar = grammars.get(i);
//            if (extendStartSymbol.equals(grammar[0])) {
//                passedGrammar[i] = true;
//                int[] firstItem = {i, 0};
//                return closure(firstItem);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * @param item
//     *            项目集中某一状态的一个项目
//     *            item[0]为文法序号，item[1]为加点的位置
//     * @return 该项的闭包
//     */
//    public List<int[]> closure(int[] item) {
//        List<int[]> itemList = new ArrayList<int[]>(); // 状态的项目集
//        itemList.add(item);
//        int grammarNum = item[0];
//        int pos = item[1];
//
//        String[] itemGrammar = grammars.get(grammarNum);
//
//        if (pos>=itemGrammar[1].length()) {
//            return itemList;
//        }
//
//        String symbol = itemGrammar[1].substring(pos, pos+1);
//
//        if (vt.contains(symbol)) { //symbol为终结符
//            return itemList;
//        } else {
//            for (int i = 0; i < grammars.size(); i++) {
//                String[] grammar = grammars.get(i);
//
//                if (symbol.equals(grammar[0])) {
//                    if (passedGrammar[i] == true) {
//                        continue;
//                    }
//                    passedGrammar[i] = true;
//                    int[] newItem = {i, 0}; //E ->   E+T
//                    if (EMPTY.equals(grammar[1])) {
//                        newItem[1] = 1;
//                        itemList.add(newItem);// 将S->ε填入set
//                        int[] addPosItem = {item[0], item[1]+1};
//                        itemList.addAll(closure(addPosItem));
//                    } else {
//                        itemList.addAll(closure(new int[] {newItem[0], newItem[1]}));
//                    }
//                }
//            }
//            return itemList;
//        }
//    }
//
//
//    private void createActionAndGoto() {
//        int count = 0;
//        actionTable = new ArrayList<Map<String, String>>(); // action表
//        gotoTable = new ArrayList<Map<String, Integer>>(); // goto表
//        for (int i = 0; i < goTable.size(); i++) {
//            Map<String, Integer> go = goTable.get(i);
//            Map<String, Integer> gotoo = new HashMap<String, Integer>();
//            Map<String, String> action = new HashMap<String, String>();
//            if (go.isEmpty()) {
//                int[] newItem = new int[2];
//                int[] item = itemLists.get(i).get(0);
//                String[] grammar = grammars.get(item[0]);
//
//                for (String terminal : vt) {
//                    action.put(terminal, "r" + item[0]);
//                }
//                action.put(OVER,  "r" + item[0]);
//
//            } else {
//                for (String key : go.keySet()) {
//                    if (vt.contains(key)) {
//                        action.put(key, "S" + go.get(key));
//                    } else {
//                        gotoo.put(key, go.get(key));
//                    }
//                }
//            }
//
//            gotoTable.add(gotoo);
//            actionTable.add(action);
//        }
//
//
//        if(startSymbol.equals(extendStartSymbol)){
//            Map<String, Integer> gotoo = new HashMap<String, Integer>();
//            Map<String, String> action = new HashMap<String, String>();
//            action.put(OVER, "acc");
//            int accState = actionTable.size();
//            actionTable.add(action);
//            gotoTable.add(gotoo);
//            gotoTable.get(INITIAL_STATE).put(extendStartSymbol, accState);
//        } else {
//            int accState = gotoTable.get(INITIAL_STATE).get(startSymbol);
//            actionTable.get(accState).clear();
//            gotoTable.get(accState).clear();
//            actionTable.get(accState).put(OVER, "acc");
//            gotoTable.get(INITIAL_STATE).put(startSymbol, accState);
//        }
//    }
//
//    public void control() {
//        Scanner input = new Scanner(System.in);
//        String inputString = "";//输入串
//        Stack<String> symbolStack = new Stack<String>();//符号栈
//        Stack<Integer> stateStack = new Stack<Integer>();//状态栈
//        String action = "";
//        int gotoo = -1;
//
//        System.out.println("\n Please Enter:");
//        inputString = input.next();
//
//        System.out.printf("%3s\t%-45s %-45s %-30s ","步骤","状态栈","符号栈","输入串");
//
//        symbolStack.push(OVER);
//        stateStack.push(INITIAL_STATE);
//        System.out.println("ACTION\tGOTO");
//        for (int stepNum = 1; !action.equals("acc"); stepNum++) {
//            System.out.printf("(%d)\t%-20s %-20s %-14s ",stepNum,stateStack.toString(),symbolStack.toString(),inputString);
//            String symbol = inputString.substring(0, 1);
//            Integer state = stateStack.peek();
//            action = actionTable.get(state).get(symbol);
//            if (action == null) {
//                System.out.println("\n error!");
//                return;
//            }
//            System.out.print(action + "\t");
//
//            String act;
//            Integer num;
//
//            if (!"acc".equals(action)) { //如果action不为acc则要继续判断是否需要求goto
//                act = action.substring(0, 1);
//                num = Integer.valueOf(action.substring(1, 2));
//                if ("r".equals(act)) { //action为归约， 需要求goto
//                    String[] grammar = grammars.get(num);
//                    int popNum = grammar[1].length();
//                    String pushSymbol = grammar[0];
//                    for (int i = 0; i < popNum; i++) {
//                        stateStack.pop();
//                        symbolStack.pop();
//                    }
//                    gotoo = gotoTable.get(stateStack.peek())
//                            .get(pushSymbol);
//                    stateStack.push(gotoo);
//                    symbolStack.push(pushSymbol);
//                    System.out.print(gotoo);
//                } else {
//                    gotoo = -1;
//                    stateStack.push(num);
//                    String newSymbol = "";
//                    if(inputString.length()>1) {
//                        newSymbol = inputString.substring(0, 1);
//                        inputString = inputString.substring(1);
//                    } else {
//                        System.out.println("\n error!");
//                        return;
//                    }
//                    symbolStack.push(newSymbol);
//                }
//
//                System.out.println();
//            } else {
//                break;
//            }
//
//        }
//        System.out.println("\n success!");
//    }
//
    
    /**
     * 打印文法
     */
    public void printGrammar() {
        System.out.println("文法：");
        for(List<String> grammar : grammars){
            for(String word : grammar){
                System.out.print(word+"_");
            }
            System.out.println();
        }
    }

    /**
     * 打印非终结符
     */
    public void printVn() {
        System.out.println("非终结符：");
        for (String c : vnSet) {
            System.out.println(c);
        }
    }

    /**
     * 打印终结符
     */
    public void printVt() {
        System.out.println("终结符：");
        for (String c : vtSet) {
            System.out.println(c);
        }
    }

//    /**
//     * 打印项目集
//     */
//    public void printItemLists() {
//        System.out.println("项目集：");
//        for (int i = 0; i < itemLists.size(); i++) {
//            System.out.println("I" + i + ":");
//            List<int[]> itemList = itemLists.get(i);
//            for (int[] item : itemList) {
//                int grammarNum = item[0];
//                int pos = item[1];
//                String[] grammar = grammars.get(grammarNum);
//                if(EMPTY.equals(grammar[1])){
//                    System.out.println("\t" + grammar[0] + LINK + "·");
//                } else {
//                    System.out.println("\t" + grammar[0] + LINK
//                            + grammar[1].substring(0, pos) + "·"
//                            + grammar[1].substring(pos));
//                }
//            }
//            Map<String, Integer> map = goTable.get(i);
//            for (String key : map.keySet()) {
//                System.out.println("\t\t" + key + LINK + map.get(key));
//            }
//        }
//        System.out.println();
//    }
//
//    /**
//     * 打印Action表
//     */
//    public void printActionAndGotoTable() {
//        System.out.println("      action and goto");
//        System.out.print("      ");
//        for (String s : vt) {
//            System.out.print(s+"   ");
//        }
//        System.out.print(OVER+"   ");
//        for (String s : vn) {
//            System.out.print(s+"   ");
//        }
//        System.out.println();
//        int count = 0;
//        for (int i=0; i<actionTable.size(); i++) {
//            Map<String, String> actionItem = actionTable.get(i);
//            Map<String, Integer> gotoItem = gotoTable.get(i);
//            String itemSetsNum = "I" + count;
//            System.out.printf("%-4s",itemSetsNum);
//            //if(actionItem.size()>0)
//            System.out.print("  ");
//            for (String s : vt) {
//                if(actionItem.get(s)!=null){
//                    System.out.print(actionItem.get(s)+"  ");
//                } else {
//                    System.out.print("    ");
//                }
//            }
//            if(actionItem.get(OVER)!=null){
//                System.out.print(actionItem.get(OVER));
//            }else{
//                System.out.print("   ");
//            }
//            System.out.print(" ");
//            for (String s : vn) {
//                if(gotoItem.get(s)!=null){
//                    System.out.print(gotoItem.get(s)+"  ");
//                } else {
//                    System.out.print("    ");
//                }
//            }
//            System.out.println();
//            count++;
//        }
//    }
//
//    /**
//     * 打印go表
//     */
//    public void printGoTable() {
//        System.out.println("go表：");
//        System.out.println();
//        int count = 0;
//        for (Map<String, Integer> goItem : goTable) {
//            System.out.print("I" + count + " ");
//            System.out.println(goItem.toString());
//            count++;
//        }
//
//    }
//
//    /**
//     * 比较list<int[]>类型的对象的内容是否相同
//     *
//     * @param a
//     * @param b
//     * @return 相同返回true，不同返回false, a或b为null返回false
//     */
//    public boolean compareItemList(List<int[]> a, List<int[]> b) {
//        if (a == null || b == null) {
//            return false;
//        }
//        if (a.size() != b.size()) {
//            return false;
//        }
//        Iterator<int[]> ia = a.iterator();
//        while (ia.hasNext()) {
//            int[] sa = ia.next();
//            Iterator<int[]> ib = b.iterator();
//            boolean find = false;
//            while (ib.hasNext()) {
//                int[] sb = ib.next();
//                if (compareIntegerArray(sa, sb)) {
//                    find = true;
//                    break;
//                }
//            }
//            if (!find) {
//                return false;
//            }
//
//        }
//        return true;
//    }
//
//    /**
//     * 比较Integer[]类型的对象的内容是否相同
//     *
//     * @param a
//     * @param b
//     * @return 相同返回true，不同返回false, a或b为null返回false
//     */
//    public boolean compareIntegerArray(int[] a, int[] b) {
//        if (a == null || b == null) {
//            return false;
//        }
//        if (a.length != b.length) {
//            return false;
//        }
//        for (int i = 0; i < a.length; i++) {
//            if (a[i]!=b[i]) {
//                return false;
//            }
//        }
//        return true;
//    }

}
