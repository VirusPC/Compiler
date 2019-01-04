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
    //public static String OVER = "#"; //������
    public static String OVER = Reserve.Over.getId().toString(); //������
    public static String EMPTY = "empty"; //��
    public static String LINK = "->"; //�ķ����ӷ�
    public static String SEPARATOR = "|"; //�ķ��ָ���
    public static String WORD_SEPARATOR = "_"; //���ʷָ���
    //public static String POINT = "��"; //��Ŀ�ĵ�
    public static String EXTEND_START_SYMBOL = "extend"; //Ĭ����չ��ʼ��
    public static String SHIFT = "S"; //�ƽ�
    public static String ACC = "acc"; //���
    public static String BECOME = "r"; //��Լ
    public static int INITIAL_STATE = 0; //��ʼ״̬


    private List<List<String>> grammars; // �ķ�
    private String startSymbol;  // ��ʼ��
    private Set<String> vnSet; // ���ս������
    private Set<String> vtSet; // �ս������
    private Map<String, Integer> toEmpty; //���ս���Ƿ�����Ƴ��մ� 1�� 0δ�� -1��
    private Map<String, Set<String>> firstVn; //���з��ս����first��
    private List<Set<Item>> itemSetList; // ��Ŀ��
    private List<Map<String, Integer>> goTable; // ��Ŀ����go()

    private List<Map<String, String>> actionTable; // action
    private List<Map<String, Integer>> gotoTable; // goto��


    private Word parsedWord;
    private Stack<String> symbolStack;//����ջ
    private Stack<Integer> stateStack;//״̬ջ
    private Stack<SemanticNode> semanticStack; //����ջ
    private Map<String, Info> symbolTable;
    private List<FourElement> fourElementList;
    private List<Integer> fourElementChain;
    private Integer tempCount = 1;
    private Integer fourElementCount=0;



    /**
     * ��ȡ�ļ��������ļ���������vn��vt Ҫ�󣺷���LR(0)�ķ�����
     * @param path �ļ�·��
     * @param startSymbol ��ʼ��
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
        if(vtSet.contains(parseV) || parseV.equals(OVER)){
            firstSet.add(parseV);

            // 2.Ҫ�����ķ���Ϊ���ս��
        }else if(vnSet.contains(parseV)){
            int oldSize = -1;
            //while(oldSize!=firstSet.size()){
                oldSize = firstSet.size();

                //�ҵ��Ը÷���Ϊ�󲿵��ķ�
                for(List<String> grammar : grammars){
                    if(!parseV.equals(grammar.get(0))){
                        continue;
                    }
                    int pos=1;
                    String parseVInRight = grammar.get(pos);
                    if(parseV.equals(parseVInRight)){
                        continue;
                    }
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
            //}
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
        if(firstItem.getGrammarId() == 5){
            int i = 1;
        }
        List<String> grammar = grammars.get(firstItem.getGrammarId());
        //��λ�����ʱ��ֱ�ӷ���
        if(firstItem.getpointPos()>=grammar.size()){
            return itemSet;

        }

        //ȡ�õ��ĵ�һ������
        String vAfterPoint = grammar.get(firstItem.getpointPos());
        Set<String> newForwards = new HashSet<String>();

        // ֻ���ڵ���һ������Ϊ���ս��ʱ���ż�����հ�
        //if(vAfterPoint))
        if(vnSet.contains(vAfterPoint)){
            // ȡ�ķ��и÷��ź�������з��ţ� ��forwardƴ�Ӻ� ������first�� ��ȡ������Ϊ�µ�forward��
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
            // �ҵ������Ը÷��ս��Ϊ�󲿵��﷨������newForwardsһ������µ���Ŀ��������Ŀ��
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
     * ������Ŀ���б�
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
            //������Ŀ��
            for (Item item : itemSet) {
                List<String> grammar = grammars.get(item.getGrammarId());
                //����ʱ����պ�λ���﷨���һ�����ŵĺ��档������Щ�﷨
                if (item.getpointPos()>=grammar.size()) {
                    continue;
                }
                //��ȡ�����ķ���,��Ϊ������
                String parseWord = grammar.get(item.getpointPos());
                if(parseWord.equals(EMPTY)){
                    continue;
                }
                //����Ƶ�����Ŀ
                Item newItem = new Item(item.getGrammarId(), item.getpointPos()+1, item.getForwards());

                /* ��go�����ڸ÷��ţ�����루���ţ������Ŀ����ţ� ����������Ŀ�ıհ�����itemSetList
                 * �����ڣ��ҵ��÷���ָ�����Ŀ������������Ŀ�ıհ�����
                 */
                if (!go.containsKey(parseWord)) {
                    go.put(parseWord, itemSetList.size());
                    itemSetList.add(closure(newItem));
                } else {
                    int nextItemListNum = go.get(parseWord);
                    itemSetList.get(nextItemListNum).addAll(closure(newItem));
                }

            }

            //���½�����Ŀ����֮ǰ����Ŀ���Ƚϣ��ж��Ƿ���ͬ
            //������������Ŀ��
            for (int numOfNewItemSet = itemSetListCountAtBegin; numOfNewItemSet < itemSetList.size(); numOfNewItemSet++) {
                Set<Item> newItemSet = itemSetList.get(numOfNewItemSet);
                for (int numOfOldItemSet = 0; numOfOldItemSet < itemSetListCountAtBegin; numOfOldItemSet++) {
                    Set<Item> oldItemSet = itemSetList.get(numOfOldItemSet);
                    //��������Ŀ�������Ŀ����ͬʱ���ض���go����ָ������Ŀ���ķ��ţ�ʹ��ָ�����Ŀ����ɾ������Ŀ��,
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
        } // ����itemList����

        //for(){}

    }

    /**
     * ����ACTION���GOTO��
     */
    public void createActionAndGoto(){
        actionTable = new ArrayList<Map<String, String>>();
        gotoTable = new ArrayList<Map<String, Integer>>();
        // ����goTable
        for(int i=0; i<goTable.size(); i++){
            Map<String, String> actionItem = new HashMap<String, String>();
            Map<String, Integer> gotoItem = new HashMap<String, Integer>();

            //��д GOTO���ACTION���е��ƽ�
            Map<String, Integer> go = goTable.get(i);
            for(String parseWord : go.keySet()){
                if(vtSet.contains(parseWord)){
                    actionItem.put(parseWord, SHIFT+go.get(parseWord).toString());
                }else{
                    gotoItem.put(parseWord, go.get(parseWord));
                }
            }

            //��дACTION���еĹ�Լ
            Set<Item> itemSet= itemSetList.get(i);
            for(Item item : itemSet){
                List<String> grammar = grammars.get(item.getGrammarId());
                //���������﷨�Ҳ�Ϊ��ʱ��Լ
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


            //����ACITON���GOTO���һ��
            actionTable.add(actionItem);
            gotoTable.add(gotoItem);
        }
    }


    /**
     * ��������ַ���(�����ַ�ΪԪ��)�����������
     */
    public void control() {
        Scanner input = new Scanner(System.in);
        String inputString = "";//���봮
        Stack<String> symbolStack = new Stack<String>();//����ջ
        Stack<Integer> stateStack = new Stack<Integer>();//״̬ջ

        String action = "";
        int gotoo = -1;

        System.out.println("\n Please Enter:");
        inputString = input.next();

        System.out.printf("%3s\t%-45s %-45s %-30s ","����","״̬ջ","����ջ","���봮");

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

            if (!ACC.equals(action)) { //���action��Ϊacc��Ҫ�����ж��Ƿ���Ҫ��goto
                act = action.substring(0, 1);
                num = Integer.valueOf(action.substring(1));
                if (BECOME.equals(act)) { //actionΪ��Լ�� ��Ҫ��goto
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



    //����������
    public void parseWordStream(List<Word> wordStream) {
        symbolStack = new Stack<String>();//����ջ
        stateStack = new Stack<Integer>();//״̬ջ
        semanticStack = new Stack<SemanticNode>();//����ջ
        fourElementList = new ArrayList<FourElement>();//��Ԫʽ�б�
        symbolTable = new HashMap<String, Info>(); //���ű�

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

            if (!ACC.equals(action)) { //���action��Ϊacc��Ҫ�����ж��Ƿ���Ҫ��goto
                act = action.substring(0, 1);
                num = Integer.valueOf(action.substring(1));
                if (BECOME.equals(act)) { //actionΪ��Լ
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

                        //Ϊ��ʶ������ʱ, Ϊ����ʱ��������ջ
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
                            //����������ջ
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
     * ����Ŀ����Ԫʽ��result(��ת��ַ)
     * @param target
     * @param result
     */
    private void backpatch(int target, int result){
        fourElementList.get(target).setResult(String.valueOf(result));
    }


    private void subroutine(Integer id){
        switch (id) {
            //��ֵ
            case 2:
                SemanticNode  arithmetic= semanticStack.pop();
                SemanticNode identifier = semanticStack.pop();
                if(!arithmetic.getType().equals(Reserve.Int.getId())){
                    System.out.println("need integer!");
                }
                generate("=", arithmetic.getPalce(), "_", identifier.getPalce());
                break;
            //��
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
            //��
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
            //��
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
                //��
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
            case 13://���ͱ�������
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
    /**
     * ��ӡAction��
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
     * ��ӡgo��
     */
    public void printGoTable() {
        System.out.println("go��");
        System.out.println();
        int count = 0;
        for (Map<String, Integer> goItem : goTable) {
            System.out.print("I" + count + " ");
            System.out.println(goItem.toString());
            count++;
        }

    }



}
