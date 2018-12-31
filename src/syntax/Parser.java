package syntax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Parser {
    public static String OVER = "#"; //������
    public static String EMPTY = "��"; //��
    public static String LINK = "->"; //�ķ����ӷ�
    public static String SEPARATOR = "|"; //�ķ��ָ���
    public static String WORD_SEPARATOR = "_"; //���ʷָ���
    public static String POINT = "��"; //��Ŀ�ĵ�
    public static String EXTEND_START_SYMBOL = "extend"; //Ĭ����չ��ʼ��
    public static int INITIAL_STATE = 0; //��ʼ״̬

    private List<List<String>> grammars; // �ķ�
    private String startSymbol;  // ��ʼ��
    private Set<String> vnSet; // ���ս������
    private Set<String> vtSet; // �ս������
    private Map<String, Integer> toEmpty; //���ս���Ƿ�����Ƴ��մ� 1�� 0δ�� -1��
    private Map<String, Set<String>> firstVn; //���з��ս����first��

    private List<Set<Item>> itemSets; // ��Ŀ��
    private List<Map<String, Integer>> goTable; // ��Ŀ����go()
    private boolean[] passedGrammar; // ����߹���·��

    private List<Map<String, String>> actionTable; // action��
    private List<Map<String, Integer>> gotoTable; // goto��

    /**
     * ��ȡ�ļ��������ļ���������vn��vt Ҫ�󣺷���LR(0)�ķ�����
     * @param path �ļ�·��
     * @param startSymbol ��ʼ��
     */
    public Parser(String path, String startSymbol) {
        createGrammars(path, startSymbol);
        fillToEmpty();
        createFirstForVn();
        List<String> ab = new ArrayList<String>();
        ab.add("A");
        ab.add("B");
        Set<String> abSet = first(ab);
        int a =1;
//            init(path, startSymbol);
//            extend();
//            createItemLists();
//            createActionAndGoto();
    }

//    public Parser(String path) {
//        this(path, DEFALT_START_SYMBOL);
//    }
    
    /**
     * �����ķ�
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
             * ���ж�ȡ�ķ����ķ���ʽΪsymbol[0]->symbol[1], symbol[0]Ϊ�ս��
             * symbol[1]Ϊ�ս������ս����ɵı��ʽ
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

                // ��->��ߵķ��ż��뵽���ս��������
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
            // �����ս�����ս���������޳�
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
     * ��չ�ķ�
     */
    private void extend() {
        List<String> grammar = new ArrayList<String>();
        grammar.add(EXTEND_START_SYMBOL);
        grammar.add(startSymbol);
        grammars.add(0, grammar);
        vnSet.add(EXTEND_START_SYMBOL);
    }


    /**
     * ������Ƴ�EMPTY�ķ��ս��������toEmpty
     *  0δ�� 1�� -1��
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
         * 1. ɾ�������Ҳ������ս���Ĳ���ʽ������ʹ����ĳһ���ս��Ϊ�󲿵����в���ʽ����ɾ����
         * ��toEmpty�ж�Ӧ�÷��ս���ı��ֵ��Ϊ��-1����˵���÷��ս�������Ƴ���
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
         * 2. ��ĳһ���ս����ĳһ����ʽ�Ҳ�ΪEMPTY����toEmpty�ж�Ӧ�÷��ս���ı�־��Ϊ�ǣ�1����
         * �����ķ���ɾ���÷��ս�������в���ʽ��
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
         * 3. �������������裬ɾ�����Ҳ�Ϊ�ջ��Ҳ����ս�����﷨����ʣ�µ�Ϊ�Ҳ�ȫ��Ϊ���ս�����﷨
         *  ��������ɨ�����ʽ�Ҳ���ÿһ����
         */
        boolean changed = true;
        while (changed) {
            changed = false;
            /**  3.1 ��ɨ�赽�ķ��ս����toEmpty�ж�Ӧ�ı�־�ǡ��ǡ���1������ɾȥ�÷��ս����
             ����ʹ�ò���ʽ�Ҳ�Ϊ�գ��򽫲���ʽ�󲿵ķ��ս����toEmpty�ж�Ӧ�ı�־λ��Ϊ���ǡ���1����
             ��ɾ���Ը÷��ս��Ϊ�󲿵����в���ʽ
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


            /**   3.2����ɨ�赽�ķ��ս������toEmpty�ж�Ӧ�ı�־�ǡ��񡱣���ɾȥ�ò���ʽ��
             ����ʹ����ʽ�󲿷��ս�����йز���ʽ����ɾȥ�������toEmpty�и÷��ս����Ӧ�ı�־��Ϊ���񡱣�-1��
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
     * ��ÿһ���ķ�����X����V������FIRST(X)
     * @param parseV
     * @return
     */
    private Set<String> firstForOneV(String parseV){
        Set<String> firstSet = new HashSet<String>();
        // 1.Ҫ�����ķ���Ϊ�ս��
        if(vtSet.contains(parseV)){
            firstSet.add(parseV);

            // 2.Ҫ�����ķ���Ϊ���ս��
        }else if(vnSet.contains(parseV)){
            int oldSize = -1;
            while(oldSize!=firstSet.size()){
                oldSize = firstSet.size();

                //�ҵ��Ը÷���Ϊ�󲿵��ķ�
                for(List<String> grammar : grammars){
                    if(!parseV.equals(grammar.get(0))){
                        continue;
                    }
                    int pos=1;
                    String parseVInRight = grammar.get(pos);
                    // 2. && 3. �Ҳ���һ��Ϊ�ս�����
                    if(EMPTY.equals(parseVInRight) || vtSet.contains(parseVInRight)){
                        firstSet.add(parseVInRight);
                    } else {//4.�Ҳ���һ��Ϊ���ս��

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
            }
        }
        return firstSet;
    }


    /**
     * �������з��ս����first��
     */
    private void createFirstForVn(){
        firstVn = new HashMap<String, Set<String>>();
        for(String v : vnSet) {
            firstVn.put(v, firstForOneV(v));
        }
    }

    /**
     * ������Ŵ���first��
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
     * �հ�����
     */
    private Set<Item> closure(Item firstItem){
        Set<Item> itemSet = new HashSet<Item>();
        itemSet.add(firstItem);



        //for

        return itemSet;
    }

    /**
     * ������Ŀ���б�
     */
    private void createItemSets(){
        Item initItem = new Item(0, 0, "#");
        Set<Item> firstItemSet = closure(initItem);
        itemSets = new ArrayList<Set<Item>>();
        itemSets.add(firstItemSet);

        //for(){}

    }

    //    /**
//     * ������Ŀ��
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
//            //������Ŀ��
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
//            //���½�����Ŀ����֮ǰ����Ŀ���Ƚϣ��ж��Ƿ���ͬ
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
//        } // ����itemList����
//
//    }
//
//    /**
//     * @return ��һ��״̬����Ŀ��
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
//     *            ��Ŀ����ĳһ״̬��һ����Ŀ
//     *            item[0]Ϊ�ķ���ţ�item[1]Ϊ�ӵ��λ��
//     * @return ����ıհ�
//     */
//    public List<int[]> closure(int[] item) {
//        List<int[]> itemList = new ArrayList<int[]>(); // ״̬����Ŀ��
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
//        if (vt.contains(symbol)) { //symbolΪ�ս��
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
//                        itemList.add(newItem);// ��S->������set
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
//        actionTable = new ArrayList<Map<String, String>>(); // action��
//        gotoTable = new ArrayList<Map<String, Integer>>(); // goto��
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
//        String inputString = "";//���봮
//        Stack<String> symbolStack = new Stack<String>();//����ջ
//        Stack<Integer> stateStack = new Stack<Integer>();//״̬ջ
//        String action = "";
//        int gotoo = -1;
//
//        System.out.println("\n Please Enter:");
//        inputString = input.next();
//
//        System.out.printf("%3s\t%-45s %-45s %-30s ","����","״̬ջ","����ջ","���봮");
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
//            if (!"acc".equals(action)) { //���action��Ϊacc��Ҫ�����ж��Ƿ���Ҫ��goto
//                act = action.substring(0, 1);
//                num = Integer.valueOf(action.substring(1, 2));
//                if ("r".equals(act)) { //actionΪ��Լ�� ��Ҫ��goto
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
     * ��ӡ�ķ�
     */
    public void printGrammar() {
        System.out.println("�ķ���");
        for(List<String> grammar : grammars){
            for(String word : grammar){
                System.out.print(word+"_");
            }
            System.out.println();
        }
    }

    /**
     * ��ӡ���ս��
     */
    public void printVn() {
        System.out.println("���ս����");
        for (String c : vnSet) {
            System.out.println(c);
        }
    }

    /**
     * ��ӡ�ս��
     */
    public void printVt() {
        System.out.println("�ս����");
        for (String c : vtSet) {
            System.out.println(c);
        }
    }

//    /**
//     * ��ӡ��Ŀ��
//     */
//    public void printItemLists() {
//        System.out.println("��Ŀ����");
//        for (int i = 0; i < itemLists.size(); i++) {
//            System.out.println("I" + i + ":");
//            List<int[]> itemList = itemLists.get(i);
//            for (int[] item : itemList) {
//                int grammarNum = item[0];
//                int pos = item[1];
//                String[] grammar = grammars.get(grammarNum);
//                if(EMPTY.equals(grammar[1])){
//                    System.out.println("\t" + grammar[0] + LINK + "��");
//                } else {
//                    System.out.println("\t" + grammar[0] + LINK
//                            + grammar[1].substring(0, pos) + "��"
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
//     * ��ӡAction��
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
//     * ��ӡgo��
//     */
//    public void printGoTable() {
//        System.out.println("go��");
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
//     * �Ƚ�list<int[]>���͵Ķ���������Ƿ���ͬ
//     *
//     * @param a
//     * @param b
//     * @return ��ͬ����true����ͬ����false, a��bΪnull����false
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
//     * �Ƚ�Integer[]���͵Ķ���������Ƿ���ͬ
//     *
//     * @param a
//     * @param b
//     * @return ��ͬ����true����ͬ����false, a��bΪnull����false
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
