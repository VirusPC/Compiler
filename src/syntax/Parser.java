package syntax;

import com.sun.org.apache.xerces.internal.util.SymbolTable;
import intermediate.FourElement;
import lexis.*;
import sun.awt.Symbol;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Parser {
    //public static String OVER = "#"; //结束符
    public static String OVER = Reserve.Over.getId().toString(); //结束符
    public static String EMPTY = "empty"; //空
    public static String LINK = "->"; //文法连接符
    public static String SEPARATOR = "|"; //文法分隔符
    public static String WORD_SEPARATOR = "_"; //单词分隔符
    //public static String POINT = "·"; //项目的点
    public static String EXTEND_START_SYMBOL = "extend"; //默认拓展开始符
    public static String SHIFT = "S"; //移进
    public static String ACC = "acc"; //完成
    public static String BECOME = "r"; //规约
    public static int INITIAL_STATE = 0; //初始状态


    private List<List<String>> grammars; // 文法
    private String startSymbol;  // 开始符
    private Set<String> vnSet; // 非终结符集合
    private Set<String> vtSet; // 终结符集合
    private Map<String, Integer> toEmpty; //非终结符是否可以推出空串 1是 0未定 -1否
    private Map<String, Set<String>> firstVn; //所有非终结符的first集
    private List<Set<Item>> itemSetList; // 项目集
    private List<Map<String, Integer>> goTable; // 项目集的go()

    private List<Map<String, String>> actionTable; // action
    private List<Map<String, Integer>> gotoTable; // goto表


    private Word parsedWord;
    private Stack<String> symbolStack;//符号栈
    private Stack<Integer> stateStack;//状态栈
    private Stack<SemanticNode> semanticStack; //语义栈
    private Map<String, Info> symbolTable;
    private List<FourElement> fourElementList;
    private List<Integer> fourElementChain;
    private Integer tempCount = 1;
    private Integer fourElementCount=0;



    /**
     * 读取文件，根据文件内容生成vn、vt 要求：符合LR(0)文法规则
     * @param path 文件路径
     * @param startSymbol 起始符
     */
    public Parser(String path, String startSymbol) {
        createGrammars(path, startSymbol);
        fillToEmpty();
        createFirstForVn();
        createItemSetList();
        createActionAndGoto();
        int i = 1;
    }


    /**
     * 读入文法
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


    /**
     * 求出能推出EMPTY的非终结符，填入toEmpty
     *  0未定 1是 -1否
     */
    private void fillToEmpty() {
        toEmpty = new HashMap<String, Integer>();
        for (String word : vnSet) {
            toEmpty.put(word, 0);
        }

        List<List<String>> copyOfGrammars = new ArrayList<List<String>>();
        for(List<String> grammar: grammars){
            copyOfGrammars.add(new ArrayList<String>(grammar));
        }

        /**
         * 1. 删除所有右部含有终结符的产生式，若这使得以某一非终结符为左部的所有产生式都被删除，
         * 则将toEmpty中对应该非终结符的标记值置为否（-1），说明该非终结符不能推出空
         */
        for (int i=0; i<copyOfGrammars.size(); i++) {
            List<String> grammar = copyOfGrammars.get(i);
           for (String word : grammar) {
                if (vtSet.contains(word)) {
                    copyOfGrammars.remove(grammar);
                    i--;
                    break;
                }
            }
        }
        Set<String> reamainedVn = new HashSet<String>();
        for (List<String> grammar: copyOfGrammars) {
                reamainedVn.add(grammar.get(0));
        }
        Set<String> notToEmptyVn = new HashSet<String>(vnSet);
        notToEmptyVn.removeAll(reamainedVn);
        for (String vn : notToEmptyVn) {
            toEmpty.put(vn, -1);
        }

        /**
         * 2. 若某一非终结符的某一产生式右部为EMPTY，则将toEmpty中对应该非终结符的标志置为是（1），
         * 并从文法中删除该非终结符的所有产生式。
         */
        for (List<String> grammar : copyOfGrammars) {
            if( EMPTY.equals(grammar.get(1))){
                toEmpty.put(grammar.get(0), 1);
            }
        }
       for(String v : toEmpty.keySet()){
           if(toEmpty.get(v) == 1){
               for(int i=0; i<copyOfGrammars.size(); i++){
                   List<String> grammar = copyOfGrammars.get(i);
                   if(grammar.get(0).equals(v)){
                       copyOfGrammars.remove(i);
                       i--;
                   }
               }
           }
       }
        /**
         * 3. 经以上两个步骤，删除了右部为空或右部有终结符的语法，即剩下的为右部全部为非终结符的语法
         *  接下来，扫描产生式右部的每一符号
         */
        boolean changed = true;
        while (changed) {
            changed = false;
            /**  3.1 若扫描到的非终结符在toEmpty中对应的标志是“是”（1），则删去该非终结符；
             若这使得产生式右部为空，则将产生式左部的非终结符在toEmpty中对应的标志位改为“是”（1），
             并删除以该非终结符为左部的所有产生式
             */
            for (int i = 0; i < copyOfGrammars.size(); i++) {
                List<String> grammar = copyOfGrammars.get(i);
                for (int j = 1; j < grammar.size(); j++) {
                    String word = grammar.get(j);
                    if (toEmpty.get(word) == 1) {
                        grammar.remove(j);
                        j--;
                    }
                    if (grammar.size() == 1) {
                        changed = true;
                        String w = grammar.get(0);
                        toEmpty.put(w, 1);
                        for (int k = 0; k < copyOfGrammars.size(); k++) {
                            List<String> g = copyOfGrammars.get(k);
                            if (g.get(0).equals(w)) {
                                copyOfGrammars.remove(k);
                                k--;
                            }
                        }
                    }
                }
            }


            /**   3.2若所扫描到的非终结符号在toEmpty中对应的标志是“否”，则删去该产生式；
             若这使产生式左部非终结符的有关产生式都被删去，则把在toEmpty中该非终结符对应的标志改为“否”（-1）
             */
            List<List<String>> deletedGrammars = new ArrayList<List<String>>();
            Set<String> deletedV = new HashSet<String>();
            for (int i = 0; i < copyOfGrammars.size(); i++) {
                List<String> grammar = copyOfGrammars.get(i);
                for (int j = 1; j < grammar.size(); j++) {
                    String word = grammar.get(j);
                    if (toEmpty.get(word) == -1) {
                        deletedGrammars.add(grammar);
                        deletedV.add(grammar.get(0));
                    }

                }
            }
            copyOfGrammars.removeAll(deletedGrammars);

            reamainedVn.clear();
            for (List<String> grammar : copyOfGrammars) {
                reamainedVn.add(grammar.get(0));
            }
            deletedV.removeAll(reamainedVn);
            if(!deletedV.isEmpty()) {
                changed = true;
                for (String v : deletedV) {
                    toEmpty.put(v, -1);
                }
            }
        }
    }


    /**
     * 对每一个文发符号X属于V，计算FIRST(X)
     * @param parseV
     * @return
     */
    private Set<String> firstForOneV(String parseV){
        Set<String> firstSet = new HashSet<String>();
        // 1.要分析的符号为终结符
        if(vtSet.contains(parseV) || parseV.equals(OVER)){
            firstSet.add(parseV);

            // 2.要分析的符号为非终结符
        }else if(vnSet.contains(parseV)){
            int oldSize = -1;
            //while(oldSize!=firstSet.size()){
                oldSize = firstSet.size();

                //找到以该符号为左部的文法
                for(List<String> grammar : grammars){
                    if(!parseV.equals(grammar.get(0))){
                        continue;
                    }
                    int pos=1;
                    String parseVInRight = grammar.get(pos);
                    if(parseV.equals(parseVInRight)){
                        continue;
                    }
                    // 2. && 3. 右部第一个为终结符或空
                    if(EMPTY.equals(parseVInRight) || vtSet.contains(parseVInRight)){
                        firstSet.add(parseVInRight);
                    } else {//4.右部第一个为非终结符

                        while(vnSet.contains(parseVInRight)
                                &&toEmpty.get(parseVInRight)==1
                                &&pos<grammar.size()-1){
                            Set<String> subSet = firstForOneV((parseVInRight));
                            subSet.remove(EMPTY);
                            firstSet.addAll(subSet);
                            parseVInRight = grammar.get(++pos);
                        }
                        firstSet.addAll(firstForOneV(parseVInRight));
                    }
                }
            //}
        }
        return firstSet;
    }


    /**
     * 创建所有非终结符的first集
     */
    private void createFirstForVn(){
        firstVn = new HashMap<String, Set<String>>();
        for(String v : vnSet) {
            firstVn.put(v, firstForOneV(v));
        }
    }

    /**
     * 任意符号串的first集
     */
    private Set<String> first(List<String> wordList){
        Set<String> firstSet = new HashSet<String>();
        int pos = 0;
        String parseWord = wordList.get(pos);
        if(EMPTY.equals(parseWord)){
            firstSet.add(EMPTY);
        } else {
            while (pos<wordList.size()-1 && vnSet.contains(parseWord) && toEmpty.get(parseWord)==1 ) {
                Set<String> s = firstVn.get(parseWord);
                s.remove(EMPTY);
                firstSet.addAll(s);
                parseWord = wordList.get(++pos);
            }
            if(vnSet.contains(parseWord)){
                firstSet.addAll(firstVn.get(parseWord));
            } else {
                firstSet.add(parseWord);
            }


        }
        return firstSet;
    }

    /**
     * 闭包函数
     */
    private Set<Item> closure(Item firstItem){
        Set<Item> itemSet = new HashSet<Item>();
        itemSet.add(firstItem);
        if(firstItem.getGrammarId() == 5){
            int i = 1;
        }
        List<String> grammar = grammars.get(firstItem.getGrammarId());
        //点位于最后时，直接返回
        if(firstItem.getpointPos()>=grammar.size()){
            return itemSet;

        }

        //取得点后的第一个符号
        String vAfterPoint = grammar.get(firstItem.getpointPos());
        Set<String> newForwards = new HashSet<String>();

        // 只有在点后第一个符号为非终结符时，才继续求闭包
        //if(vAfterPoint))
        if(vnSet.contains(vAfterPoint)){
            // 取文法中该符号后面的所有符号， 与forward拼接后， 对其求first集 。取并集作为新的forward集
            List<String> remainedVs = grammar.subList(firstItem.getpointPos()+1, grammar.size());
            for(String oldForward : firstItem.getForwards()){
                List<String> jointVs = new ArrayList<String>(remainedVs);
                jointVs.add(oldForward);
                newForwards.addAll(first(jointVs));
            }
//            if(forwards.size()<=1 && forwards.toArray()[0].equals(EMPTY)){
//                forwards.clear();
//                forwards.add(OVER);
//            }
            // 找到所有以该非终结符为左部的语法，并与newForwards一起组成新的项目，加入项目集
            for(int i=0; i<grammars.size(); i++){
                List<String> nextGrammar = grammars.get(i);
                if(vAfterPoint.equals(nextGrammar.get(0))){
                    Item newItem = new Item(i, 1, newForwards);
                    //itemSet.add(newItem);
                    //if(vnSet.contains(nextGrammar.get(1))){
                        itemSet.addAll(closure(newItem));
                   // }
                }
            }

        }




        return itemSet;
    }

    /**
     * 创建项目集列表
     */
    private void createItemSetList(){
        Set<String> firstForwards = new HashSet<String>();
        firstForwards.add(OVER);
        Item initItem = new Item(0, 1, firstForwards);
        Set<Item> firstItemSet = closure(initItem);
        itemSetList = new ArrayList<Set<Item>>();
        itemSetList.add(firstItemSet);

        goTable = new ArrayList<Map<String, Integer>>();
//        Map<String, Integer> firstGo = new HashMap<String, Integer>();
//        goTable.add(firstGo);

        for (int numOfParsedItemSet = 0; numOfParsedItemSet < itemSetList.size(); numOfParsedItemSet++) {
            Set<Item> itemSet = itemSetList.get(numOfParsedItemSet);
            Map<String, Integer> go = new HashMap<String, Integer>();
            int itemSetListCountAtBegin = itemSetList.size();

            if(numOfParsedItemSet == 3){
                int i123 = 1;
            }
            //创建项目集
            for (Item item : itemSet) {
                List<String> grammar = grammars.get(item.getGrammarId());
                //等于时，点刚好位于语法最后一个符号的后面。跳过这些语法
                if (item.getpointPos()>=grammar.size()) {
                    continue;
                }
                //获取点后面的符号,若为空跳过
                String parseWord = grammar.get(item.getpointPos());
                if(parseWord.equals(EMPTY)){
                    continue;
                }
                //点后移得新项目
                Item newItem = new Item(item.getGrammarId(), item.getpointPos()+1, item.getForwards());

                /* 若go表不存在该符号，则加入（符号，后继项目集编号） ，并将该项目的闭包加入itemSetList
                 * 若存在，找到该符号指向的项目集，并将该项目的闭包加入
                 */
                if (!go.containsKey(parseWord)) {
                    go.put(parseWord, itemSetList.size());
                    itemSetList.add(closure(newItem));
                } else {
                    int nextItemListNum = go.get(parseWord);
                    itemSetList.get(nextItemListNum).addAll(closure(newItem));
                }

            }

            //将新建的项目集与之前的项目集比较，判断是否相同
            //遍历新增的项目集
            for (int numOfNewItemSet = itemSetListCountAtBegin; numOfNewItemSet < itemSetList.size(); numOfNewItemSet++) {
                Set<Item> newItemSet = itemSetList.get(numOfNewItemSet);
                for (int numOfOldItemSet = 0; numOfOldItemSet < itemSetListCountAtBegin; numOfOldItemSet++) {
                    Set<Item> oldItemSet = itemSetList.get(numOfOldItemSet);
                    //存在新项目集与旧项目集相同时，重定向go表中指向新项目集的符号，使其指向旧项目集。删掉新项目集,
                    if (newItemSet.equals(oldItemSet)) {
                       // if(numOfNewItemSet == 8){
                            int i = 1;
                       // }
                        for (String key : go.keySet()) {
                            Integer numOfNewItemSetFromGo = go.get(key);
                            if (numOfNewItemSetFromGo.equals(numOfNewItemSet)) {
                                go.put(key, numOfOldItemSet);
                            } else if (numOfNewItemSetFromGo > numOfNewItemSet) {
                                go.put(key, numOfNewItemSetFromGo - 1);
                            }
                        }
                        itemSetList.remove(numOfNewItemSet);
                        numOfNewItemSet--;
                    }
                }
            }

            goTable.add(go);
        } // 遍历itemList结束

        //for(){}

    }

    /**
     * 创建ACTION表和GOTO表
     */
    public void createActionAndGoto(){
        actionTable = new ArrayList<Map<String, String>>();
        gotoTable = new ArrayList<Map<String, Integer>>();
        // 遍历goTable
        for(int i=0; i<goTable.size(); i++){
            Map<String, String> actionItem = new HashMap<String, String>();
            Map<String, Integer> gotoItem = new HashMap<String, Integer>();

            //填写 GOTO表和ACTION表中的移进
            Map<String, Integer> go = goTable.get(i);
            for(String parseWord : go.keySet()){
                if(vtSet.contains(parseWord)){
                    actionItem.put(parseWord, SHIFT+go.get(parseWord).toString());
                }else{
                    gotoItem.put(parseWord, go.get(parseWord));
                }
            }

            //填写ACTION表中的规约
            Set<Item> itemSet= itemSetList.get(i);
            for(Item item : itemSet){
                List<String> grammar = grammars.get(item.getGrammarId());
                //点在最后或语法右部为空时规约
                if(grammar.size() == item.getpointPos()
                        || grammar.get(1).equals(EMPTY)){
                    if(grammar.get(0).equals(EXTEND_START_SYMBOL)){
                        actionItem.put(OVER, ACC);
                        continue;
                    }
                    for(String forward:item.getForwards()){
                        actionItem.put(forward, BECOME+item.getGrammarId().toString());
                    }
                }
            }


            //填入ACITON表和GOTO表的一行
            actionTable.add(actionItem);
            gotoTable.add(gotoItem);
        }
    }


    /**
     * 输入测试字符串(单个字符为元素)，输出分析表
     */
    public void control() {
        Scanner input = new Scanner(System.in);
        String inputString = "";//输入串
        Stack<String> symbolStack = new Stack<String>();//符号栈
        Stack<Integer> stateStack = new Stack<Integer>();//状态栈

        String action = "";
        int gotoo = -1;

        System.out.println("\n Please Enter:");
        inputString = input.next();

        System.out.printf("%3s\t%-45s %-45s %-30s ","步骤","状态栈","符号栈","输入串");

        symbolStack.push(OVER);
        stateStack.push(INITIAL_STATE);
        System.out.println("ACTION\tGOTO");
        for (int stepNum = 1; !action.equals(ACC); stepNum++) {
            System.out.printf("(%d)\t%-20s %-20s %-14s ",stepNum,stateStack.toString(),symbolStack.toString(),inputString);
            String symbol = inputString.substring(0, 1);
            Integer state = stateStack.peek();
            action = actionTable.get(state).get(symbol);
            if (action == null) {
                System.out.println("\n error!");
                return;
            }
            System.out.print(action + "\t");

            String act;
            Integer num;

            if (!ACC.equals(action)) { //如果action不为acc则要继续判断是否需要求goto
                act = action.substring(0, 1);
                num = Integer.valueOf(action.substring(1));
                if (BECOME.equals(act)) { //action为归约， 需要求goto
                    List<String> grammar = grammars.get(num);
                    int popNum = grammar.size()-1;
                    String pushSymbol = grammar.get(0);
                    for (int i = 0; i < popNum; i++) {
                        stateStack.pop();
                        symbolStack.pop();
                    }
                    gotoo = gotoTable.get(stateStack.peek())
                            .get(pushSymbol);
                    stateStack.push(gotoo);
                    symbolStack.push(pushSymbol);
                    System.out.print(gotoo);
                } else {
                    gotoo = -1;
                    stateStack.push(num);
                    String newSymbol = "";
                    if(inputString.length()>1) {
                        newSymbol = inputString.substring(0, 1);
                        inputString = inputString.substring(1);
                    } else {
                        System.out.println("\n error!");
                        return;
                    }
                    symbolStack.push(newSymbol);
                }

                System.out.println();
            } else {
                break;
            }

        }
        System.out.println("\n success!");
    }



    //分析单词流
    public void parseWordStream(List<Word> wordStream) {
        symbolStack = new Stack<String>();//符号栈
        stateStack = new Stack<Integer>();//状态栈
        semanticStack = new Stack<SemanticNode>();//语义栈
        fourElementList = new ArrayList<FourElement>();//四元式列表
        symbolTable = new HashMap<String, Info>(); //符号表

       // Stack<>
        String action = "";
        int gotoo = -1;

        symbolStack.push(OVER);
        stateStack.push(INITIAL_STATE);
        for (int stepNum = 1; !action.equals(ACC); stepNum++) {
            parsedWord = wordStream.get(0);

            Integer state = stateStack.peek();
            action = actionTable.get(state).get(String.valueOf(parsedWord.getType()));
            if (action == null) {
                System.out.println("\n error!");
                return;
            }

            String act;
            Integer num;

            if (!ACC.equals(action)) { //如果action不为acc则要继续判断是否需要求goto
                act = action.substring(0, 1);
                num = Integer.valueOf(action.substring(1));
                if (BECOME.equals(act)) { //action为归约
                    subroutine(num);
                    List<String> grammar = grammars.get(num);
                    /***
                     *
                     */
                    String pushSymbol = grammar.get(0);
                    int popNum = 0;
                    if(!grammar.get(1).equals(EMPTY)){
                        popNum = grammar.size() - 1;
                        for (int i = 0; i < popNum; i++) {
                            stateStack.pop();
                            symbolStack.pop();
                        }
                    }
                    gotoo = gotoTable.get(stateStack.peek())
                            .get(pushSymbol);
                    stateStack.push(gotoo);
                    symbolStack.push(pushSymbol);

                } else {
                    gotoo = -1;
                    stateStack.push(num);
                    String newSymbol = "";
                    if(wordStream.size()>1) {

                        //为标识符或常量时, 为类型时，入语义栈
                        if(parsedWord.getValue()!=null) {
                            SemanticNode sn = new SemanticNode();
                            if(parsedWord.getType().equals(Constant.Num.getId())){
                                sn.setType(Reserve.Int.getId());
                            }else if(parsedWord.getType().equals(Constant.Char.getId())){
                                sn.setType(Reserve.Char.getId());
                            }else {
                                sn.setType(parsedWord.getType());
                            }
                            sn.setPalce(parsedWord.getValue());
                            semanticStack.push(sn);
                        }else if(parsedWord.getType() .equals( Reserve.Int.getId())
                                ||parsedWord.getType() .equals( Reserve.Char.getId())
                                ||parsedWord.getType() .equals(Reserve.Bool.getId())){
                            //类型入语义栈
                            SemanticNode sn = new SemanticNode();
                            sn.setType(parsedWord.getType());
                            semanticStack.push(sn);
                        }

                        newSymbol = String.valueOf(wordStream.get(0).getType());
                        wordStream.remove(0);
                    } else {
                        System.out.println("\n error!");
                        return;
                    }
                    symbolStack.push(newSymbol);
                }
            } else {
                break;
            }

        }



        System.out.println("\n success!");
    }


    private String newTemp(){
        return "T"+(tempCount++).toString();
    }

    private void generate(String op, String arg1, String arg2, String result){
        FourElement fourElement = new FourElement(fourElementCount++, op, arg1, arg2, result);
        fourElementList.add(fourElement);
    }

    /**
     * 回填目标四元式的result(跳转地址)
     * @param target
     * @param result
     */
    private void backpatch(int target, int result){
        fourElementList.get(target).setResult(String.valueOf(result));
    }


    private void subroutine(Integer id){
        switch (id) {
            //赋值
            case 2:
                SemanticNode  arithmetic= semanticStack.pop();
                SemanticNode identifier = semanticStack.pop();
                if(!arithmetic.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }
                generate("=", arithmetic.getPalce(), "_", identifier.getPalce());
                break;
            //加
            case 3:
                SemanticNode arg2_3 = semanticStack.pop();
                SemanticNode arg1_3 = semanticStack.pop();

                if(!arg1_3.getType().equals(arg2_3.getType()) || !arg1_3.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }

                SemanticNode arithmetic3 = new SemanticNode();
                String temp3 = newTemp();
                arithmetic3.setPalce(temp3);
                arithmetic3.setType(Reserve.Int.getId());
                //Integer value3 = Integer.valueOf(arg1_3.getPalce()) + Integer.valueOf(arg2_3.getPalce());
                generate("+", arg1_3.getPalce(), arg2_3.getPalce(), temp3);
                semanticStack.push(arithmetic3);
                break;
            //减
            case 4:
                SemanticNode arg2_4 = semanticStack.pop();
                SemanticNode arg1_4 = semanticStack.pop();
                if(!arg2_4.getType().equals(arg2_4.getType()) || !arg1_4.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }
                SemanticNode arithmetic4 = new SemanticNode();
                String temp = newTemp();
                arithmetic4.setPalce(temp);
                arithmetic4.setType(Reserve.Int.getId());
                generate("-", arg1_4.getPalce(), arg2_4.getPalce(), temp);
                semanticStack.push(arithmetic4);
                break;
            //乘
            case 6:
                SemanticNode arg2_6 = semanticStack.pop();
                SemanticNode arg1_6 = semanticStack.pop();

                if(!arg2_6.getType().equals(arg2_6.getType()) || !arg1_6.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }

                SemanticNode arithmetic6 = new SemanticNode();
                String temp6 = newTemp();
                arithmetic6.setPalce(temp6);
                arithmetic6.setType(Reserve.Int.getId());
                generate("*", arg1_6.getPalce(), arg2_6.getPalce(), temp6);
                semanticStack.push(arithmetic6);
                break;
                //除
            case 7:
                SemanticNode arg2_7 = semanticStack.pop();
                SemanticNode arg1_7 = semanticStack.pop();
                if(!arg2_7.getType().equals(arg2_7.getType()) || !arg1_7.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }
                SemanticNode arithmetic7 = new SemanticNode();
                String temp7 = newTemp();
                arithmetic7.setPalce(temp7);
                arithmetic7.setType(Reserve.Int.getId());
                generate("/", arg1_7.getPalce(), arg2_7.getPalce(), temp7);
                semanticStack.push(arithmetic7);
                break;
            case 13://整型变量定义
                SemanticNode sn13 = new SemanticNode();
                SemanticNode  arithmetic13= semanticStack.pop();
                SemanticNode identifier13 = semanticStack.pop();
                SemanticNode type13 = semanticStack.pop();

                if(!type13.getType().equals(arithmetic13.getType())){
                    System.out.println("type error!");
                }

                Info info13 = new Info();
                info13.setTypeId(type13.getType());
                symbolTable.put(identifier13.getPalce(), info13);

                generate("=", arithmetic13.getPalce(), "_", identifier13.getPalce());
                default:
        }
    }


    public void setSymbolTable(Map<String, Info> symbolTable) {
        this.symbolTable = symbolTable;
    }


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
    /**
     * 打印Action表
     */
    public void printActionAndGotoTable() {
        System.out.println("      action and goto");
        System.out.print("      ");
        for (String s : vtSet) {
            System.out.print(s+"   ");
        }
        System.out.print(OVER+"   ");
        for (String s : vnSet) {
            if(s.equals(EXTEND_START_SYMBOL)){
                System.out.print("    ");
                continue;
            }
            System.out.print(s+"   ");
        }
        System.out.println();
        int count = 0;
        for (int i=0; i<actionTable.size(); i++) {
            Map<String, String> actionItem = actionTable.get(i);
            Map<String, Integer> gotoItem = gotoTable.get(i);
            String ItemSetListNum = "I" + count;
            System.out.printf("%-4s",ItemSetListNum);
            //if(actionItem.size()>0)
            System.out.print("  ");
            for (String s : vtSet) {
                if(actionItem.get(s)!=null){
                    System.out.print(actionItem.get(s)+"  ");
                } else {
                    System.out.print("    ");
                }
            }
            if(actionItem.get(OVER)!=null){
                System.out.print(actionItem.get(OVER));
            }else{
                System.out.print("   ");
            }
            System.out.print(" ");
            for (String s : vnSet) {
                if(gotoItem.get(s)!=null){
                    System.out.print(gotoItem.get(s)+"  ");
                } else {
                    System.out.print("    ");
                }
            }
            System.out.println();
            count++;
        }
    }

    /**
     * 打印go表
     */
    public void printGoTable() {
        System.out.println("go表：");
        System.out.println();
        int count = 0;
        for (Map<String, Integer> goItem : goTable) {
            System.out.print("I" + count + " ");
            System.out.println(goItem.toString());
            count++;
        }

    }



}
