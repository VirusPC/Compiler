package syntax;

import lexis.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    private static String OVER = Reserve.Over.getId().toString();
    private static String EMPTY = "empty";//��
    private static String LINK = "->";//�ķ����ӷ�
    private static String SEPARATOR = "|"; //�ķ��ָ���
    private static String WORD_SEPARATOR = "_"; //�������ӷ�
    //public static String POINT = "��"; //��Ŀ�ĵ�
    private static String EXTEND_START_SYMBOL = "extend"; //��չ��ʼ��
    private static String SHIFT = "S"; //�ƽ�
    private static String ACC = "acc"; //���
    private static String BECOME = "r"; //��Լ
    private static int INITIAL_STATE = 0; //��ʼ״̬


    private List<List<String>> grammars; //�ķ�
    private String startSymbol;  //��ʼ��
    private Set<String> vnSet; //���ս������
    private Set<String> vtSet; //�ս������
    private Map<String, Integer> toEmpty; //���ս���Ƿ�����Ƴ��մ� 1�� 0δ�� -1��
    private Map<String, Set<String>> firstVn; //���з��ս����first��
    private List<Set<Item>> itemSetList; // ��Ŀ��
    private List<Map<String, Integer>> goTable; //��Ŀ����go()

    private List<Map<String, String>> actionTable; //action��
    private List<Map<String, Integer>> gotoTable; //goto��


    private Word parsedWord;
    private Stack<SemanticNode> semanticStack; //����ջ
    private Map<String, Info> symbolTable; //���ű�
    private List<FourElement> fourElementList; //��Ԫʽ�б�
    private Map<Integer, Integer> fourElementChain; //��Ԫʽ��
    private Integer tempCount = 1; //��ʱ��������
    private Integer nxq=0; //��һ����Ԫʽ���



    /**
     * ��ȡ�ļ��������ļ���������vn��vt Ҫ�󣺷���LR(0)�ķ�����
     * @param filePath  �ļ�·��
     * @param startSymbol ��ʼ��
     */
    public Parser(String filePath, String startSymbol) {
        readGrammars(filePath, startSymbol);
        fillToEmpty();
        createFirstSetForVn();
        createItemSetList();
        createActionAndGoto();
    }


    /**
     * �����ķ�
     *
     * @param filePath
     * @param startSymbol
     */
    private void readGrammars(String filePath, String startSymbol) {
        this.startSymbol = startSymbol;
        grammars = new ArrayList();
        vnSet = new HashSet<String>();
        vtSet = new HashSet<String>();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(filePath);
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
            //  �����ս�����ս���������޳�
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
            extendGrammars();
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
     *  ��չ�ķ�
     */
    private void extendGrammars() {
        List<String> grammar = new ArrayList<String>();
        grammar.add(EXTEND_START_SYMBOL);
        grammar.add(startSymbol);
        grammars.add(0, grammar);
        vnSet.add(EXTEND_START_SYMBOL);
    }


    /**
     * ������Ƴ�EMPTY�ķ��ս��������toEmpty
     *      *  0δ�� 1�� -1��
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
         *   ��������ɨ�����ʽ�Ҳ���ÿһ����
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


            /**  3.2����ɨ�赽�ķ��ս������toEmpty�ж�Ӧ�ı�־�ǡ��񡱣���ɾȥ�ò���ʽ��
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



    Set<String> parsed;
    /**
     * ��ÿһ���ķ�����X����V������FIRST(X)
     * @param parsedV
     * @return
     */
    private Set<String> firstForOneV(String parsedV){
        Set<String> firstSet = new HashSet<String>();
        // 1.Ҫ�����ķ���Ϊ�ս��
        if(vtSet.contains(parsedV) || parsedV.equals(OVER)){
            firstSet.add(parsedV);

            // 2.Ҫ�����ķ���Ϊ���ս��
        }else if(vnSet.contains(parsedV) && !parsed.contains(parsedV)){
            parsed.add(parsedV);
            int oldSize = -1;
            //while(oldSize!=firstSet.size()){
                oldSize = firstSet.size();

                //�ҵ��Ը÷���Ϊ�󲿵��ķ�
                for(List<String> grammar : grammars){
                    if(!parsedV.equals(grammar.get(0))){
                        continue;
                    }
                    int pos=1;
                    String parsedVInRight = grammar.get(pos);
                    if(parsedV.equals(parsedVInRight)){
                        continue;
                    }
                    // 2. && 3. �Ҳ���һ��Ϊ�ս�����
                    if(EMPTY.equals(parsedVInRight) || vtSet.contains(parsedVInRight)){
                        firstSet.add(parsedVInRight);
                    } else {//4.�Ҳ���һ��Ϊ���ս��

                            while(vnSet.contains(parsedVInRight)
                                    &&toEmpty.get(parsedVInRight)==1
                                    &&pos<grammar.size()-1
                                    &&!parsed.contains(parsedVInRight)){
                                parsed.add(parsedVInRight);
                                Set<String> subSet = firstForOneV((parsedVInRight));
                                subSet.remove(EMPTY);
                                firstSet.addAll(subSet);
                                parsedVInRight = grammar.get(++pos);
                            }
                            firstSet.addAll(firstForOneV(parsedVInRight));

                    }
                }
            //}
        }
        return firstSet;
    }


    /**
     * �������з��ս����first��
     */
    private void createFirstSetForVn(){
        firstVn = new HashMap<String, Set<String>>();
        for(String v : vnSet) {
            parsed = new HashSet<String>();
            firstVn.put(v, firstForOneV(v));
        }
    }

    /**
     * ������Ŵ���first��
     */
    private Set<String> first(List<String> wordList){
        Set<String> firstSet = new HashSet<String>();
        int pos = 0;
        String parsedWord = wordList.get(pos);
        if(EMPTY.equals(parsedWord)){
            firstSet.add(EMPTY);
        } else {
            while (pos<wordList.size()-1 && vnSet.contains(parsedWord) && toEmpty.get(parsedWord)==1 ) {
                Set<String> s = firstVn.get(parsedWord);
                s.remove(EMPTY);
                firstSet.addAll(s);
                parsedWord = wordList.get(++pos);
            }
            if(vnSet.contains(parsedWord)){
                firstSet.addAll(firstVn.get(parsedWord));
            } else {
                firstSet.add(parsedWord);
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
//        if(firstItem.getGrammarId() == 5){
//            int i = 1;
//        }
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

//            if(numOfParsedItemSet == 3){
//                int i123 = 1;
//            }
            //������Ŀ��
            for (Item item : itemSet) {
                List<String> grammar = grammars.get(item.getGrammarId());
                //����ʱ����պ�λ���﷨���һ�����ŵĺ��档������Щ�﷨
                if (item.getpointPos()>=grammar.size()) {
                    continue;
                }
                //��ȡ�����ķ���,��Ϊ������
                String parsedWord = grammar.get(item.getpointPos());
                if(parsedWord.equals(EMPTY)){
                    continue;
                }
                //����Ƶ�����Ŀ
                Item newItem = new Item(item.getGrammarId(), item.getpointPos()+1, item.getForwards());

                /* ��go�����ڸ÷��ţ�����루���ţ������Ŀ����ţ� ����������Ŀ�ıհ�����itemSetList
                 * �����ڣ��ҵ��÷���ָ�����Ŀ������������Ŀ�ıհ�����
                 */
                if (!go.containsKey(parsedWord)) {
                    go.put(parsedWord, itemSetList.size());
                    itemSetList.add(closure(newItem));
                } else {
                    int nextItemListNum = go.get(parsedWord);
                    itemSetList.get(nextItemListNum).addAll(closure(newItem));
                }

            }

            //���½�����Ŀ����֮ǰ����Ŀ���Ƚϣ��ж��Ƿ���ͬ
            // ������������Ŀ��
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
        }  // ����itemList����

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
            for(String parsedWord : go.keySet()){
                if(vtSet.contains(parsedWord)){
                    actionItem.put(parsedWord, SHIFT+go.get(parsedWord).toString());
                }else{
                    gotoItem.put(parsedWord, go.get(parsedWord));
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
        Stack<String> symbolStack = new Stack<String>();//����ջ
        Stack<Integer>  stateStack= new Stack<Integer>();//״̬ջ
        semanticStack = new Stack<SemanticNode>();//����ջ
        fourElementList = new ArrayList<FourElement>();//��Ԫʽ�б�
        fourElementChain = new HashMap<Integer,Integer>();
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

                } else {//�ƽ�
                    stateStack.push(num);
                    String newSymbol = "";
                    if(wordStream.size()>1) {
                        pushWordInSemanticStack();
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


    private void pushWordInSemanticStack(){
        //Ϊ��ʶ������ʱ, Ϊ����ʱ��������ջ
        Info info = null;
        SemanticNode sn = null;
        String name = null;

        if(parsedWord.getType().equals(Constant.Num.getId())){
            info = new Info(Kind.Constant, Reserve.Int.getId());
            name = parsedWord.getValue();
            if(symbolTable.get(name)==null){ symbolTable.put(name, info);}
            sn = new SemanticNode();
            sn.setPlace(name);
        } else if(parsedWord.getType().equals(Constant.Char.getId())){
            info = new Info(Kind.Constant, Reserve.Char.getId());
            name = parsedWord.getValue();
            if(symbolTable.get(name)==null){ symbolTable.put(name, info);}
            sn = new SemanticNode();
            sn.setPlace(name);
        } else if(parsedWord.getType().equals(Identifier.Id.getId())){
            info = new Info(Kind.Variable);
            name = parsedWord.getValue();
            //if(symbolTable.get(name)==null){ symbolTable.put(name, info);}
            sn = new SemanticNode();
            sn.setPlace(name);
        } else if(parsedWord.getType() .equals( Reserve.Int.getId())
                ||parsedWord.getType() .equals( Reserve.Char.getId())
                ||parsedWord.getType() .equals(Reserve.Bool.getId())){
            sn = new SemanticNode();
            sn.setPlace(parsedWord.getType().toString());
        }

        if(sn!=null) {
            semanticStack.push(sn);
        }
    }


    private String newTemp(){
        return "T"+(tempCount++).toString();
    }

    /**
     * ������Ԫʽ���޲���
     * @param op
     * @param result
     */
    private void generate(String op, String result){
        FourElement fourElement = new FourElement(op, "_", "_", result);
        fourElementList.add(fourElement);
        nxq++;
    }

    /**
     * ������Ԫʽ��һ������
     * @param op
     * @param arg1
     * @param result
     */
    private void generate(String op, String arg1, String result){
        FourElement fourElement = new FourElement(op, arg1, "_", result);
        fourElementList.add(fourElement);
        nxq++;
    }

    /***
     * ������Ԫʽ����������
     * @param op
     * @param arg1
     * @param arg2
     * @param result
     */
    private void generate(String op, String arg1, String arg2, String result){
        FourElement fourElement = new FourElement(op, arg1, arg2, result);
        fourElementList.add(fourElement);
        nxq++;
    }

    /**
     * ����Ŀ����Ԫʽ��result(��ת��ַ)
     * @param head
     * @param result
     */
    private void backPatch(Integer head, Integer result){
        if(head>=fourElementList.size()|| head<0 ){
            return;
        }
        String resultString = String.valueOf(result);

        while(head!=null){
			fourElementList.get(head).setResult(String.valueOf(resultString));
			head = fourElementChain.get(head);
		}
    }

    /**
     * ��rear���ӵ�front��֮��
     * @param rear
     * @param front
     * @return
     */
    private Integer merge(Integer rear, Integer front){
        Integer pos = front;
        while(fourElementChain.get(pos)!=null){
            pos = fourElementChain.get(pos);
        }
        fourElementChain.put(pos, rear);
        return front;
    }

    /**
     * ��ȡ���ű�
     * @return
     */
    public Map<String, Info> getSymbolTable(){
        return symbolTable;
    }


    public List<FourElement> getFourElementList(){
        return fourElementList;
    }

    /**
     * �����ӳ���
     * @param id �ķ����
     */
    private void subroutine(Integer grammarId){
        switch (grammarId) {
            //��ֵ
            case 2:
                SemanticNode  arithmetic= semanticStack.pop();
                SemanticNode identifier = semanticStack.pop();
				String aName = arithmetic.getPlace();
                Integer type = symbolTable.get(aName).getTypeId();
				if(!Reserve.Int.getId().equals(type)){
                    System.err.println("need integer!");
                }
				String iName = identifier.getPlace();
				if(symbolTable.get(iName)==null){
				    System.err.print(aName);
					System.err.println(" identifier not defined");
				}
				SemanticNode sn2 = new SemanticNode();
				sn2.setChain(-1);
				semanticStack.push(sn2);
                generate("=", aName, "_", iName);
                break;
            //��
            case 3:
                SemanticNode arg2_3 = semanticStack.pop();
                SemanticNode arg1_3 = semanticStack.pop();

				String name2_3 = arg2_3.getPlace();
				String name1_3 = arg1_3.getPlace();
                Integer type2_3 = symbolTable.get(name2_3).getTypeId();
				Integer type1_3 = symbolTable.get(name1_3).getTypeId();

				if(!type2_3.equals(type1_3) || !type2_3.equals(Reserve.Int.getId())){
                    System.out.println("integer only!");
                }

                SemanticNode arithmetic3 = new SemanticNode();
                String temp3 = newTemp();
                arithmetic3.setPlace(temp3);
                arithmetic3.setType(Reserve.Int.getId());
                Info info3 = new Info(Kind.Variable, Reserve.Int.getId());
				symbolTable.put(temp3, info3);
                //Integer value3 = Integer.valueOf(arg1_3.getPlace()) + Integer.valueOf(arg2_3.getPlace());
                generate("+", name1_3, name2_3, temp3);
                semanticStack.push(arithmetic3);
                break;
            //��
            case 4:
                SemanticNode arg2_4 = semanticStack.pop();
                SemanticNode arg1_4 = semanticStack.pop();
                String name2_4 = arg2_4.getPlace();
				String name1_4 = arg1_4.getPlace();
                Integer type2_4 = symbolTable.get(name2_4).getTypeId();
				Integer type1_4 = symbolTable.get(name1_4).getTypeId();

				if(!type2_4.equals(type1_4) || !type2_4.equals(Reserve.Int.getId())){
                    System.out.println("integer only!");
                }

                SemanticNode arithmetic4 = new SemanticNode();
                String temp4 = newTemp();
                arithmetic4.setPlace(temp4);
                arithmetic4.setType(Reserve.Int.getId());
                Info info4 = new Info(Kind.Variable, Reserve.Int.getId());
				symbolTable.put(temp4, info4);
                generate("-", name1_4, name2_4, temp4);
                semanticStack.push(arithmetic4);
                break;
            //��
            case 6:
                SemanticNode arg2_6 = semanticStack.pop();
                SemanticNode arg1_6 = semanticStack.pop();

                String name2_6 = arg2_6.getPlace();
				String name1_6 = arg1_6.getPlace();
                Integer type2_6 = symbolTable.get(name2_6).getTypeId();
				Integer type1_6 = symbolTable.get(name1_6).getTypeId();

				if(!type2_6.equals(type1_6) || !type2_6.equals(Reserve.Int.getId())){
                    System.out.println("integer only!");
                }

                SemanticNode arithmetic6 = new SemanticNode();
                String temp6 = newTemp();
                arithmetic6.setPlace(temp6);
                arithmetic6.setType(Reserve.Int.getId());
                Info info6 = new Info(Kind.Variable, Reserve.Int.getId());
				symbolTable.put(temp6, info6);

                generate("*", name1_6, name2_6, temp6);
                semanticStack.push(arithmetic6);
                break;
                //��
            case 7:
                SemanticNode arg2_7 = semanticStack.pop();
                SemanticNode arg1_7 = semanticStack.pop();
                String name2_7 = arg2_7.getPlace();
				String name1_7 = arg1_7.getPlace();
                Integer type2_7 = symbolTable.get(name2_7).getTypeId();
				Integer type1_7 = symbolTable.get(name1_7).getTypeId();

				if(!type2_7.equals(type1_7) || !type2_7.equals(Reserve.Int.getId())){
                    System.out.println("integer only!");
                }

                SemanticNode arithmetic7 = new SemanticNode();
                String temp7 = newTemp();
                arithmetic7.setPlace(temp7);
                arithmetic7.setType(Reserve.Int.getId());
                Info info7 = new Info(Kind.Variable, Reserve.Int.getId());
				symbolTable.put(temp7, info7);

                generate("/", name1_7, name2_7, temp7);
                semanticStack.push(arithmetic7);
                break;
//            //��ֵ
//            case 12:
//                SemanticNode sn12 = new SemanticNode();
//                sn13.setChain(0);
//                SemanticStack.push(sn13);
            case 13://���ͱ�������
                SemanticNode  arithmetic13= semanticStack.pop();
                SemanticNode identifier13 = semanticStack.pop();
                SemanticNode type13 = semanticStack.pop();

                String aName13 = arithmetic13.getPlace();
                String iName13 = identifier13.getPlace();
                Integer aType13 = symbolTable.get(aName13).getTypeId();
                Integer tType13 = Integer.valueOf(type13.getPlace());

                if(!aType13.equals(tType13)){
                    System.out.println("type error!");
                }

                Info info13 = new Info(Kind.Variable, tType13);
                symbolTable.put(iName13, info13);

                SemanticNode sn13 = new SemanticNode();
                sn13.setPlace(identifier13.getPlace());
                sn13.setChain(-1);
                semanticStack.push(sn13);
                generate("=", aName13, "_", iName13);
                break;

            //E->arithmetic
            case 16:
                SemanticNode i16 = semanticStack.pop();
                String iName16 = i16.getPlace();

                i16 = new SemanticNode();
                i16.setTc(nxq);
                i16.setFc(nxq+1);
                semanticStack.push(i16);
                generate("jnz", iName16, String.valueOf(0));
                generate("j",String.valueOf(0));
                break;
            case 17:
                SemanticNode i2_17 = semanticStack.pop();
                SemanticNode i1_17 = semanticStack.pop();
                String iName2_17 = i2_17.getPlace();
                String iName1_17 = i1_17.getPlace();

                SemanticNode e17 = new SemanticNode();
                e17.setTc(nxq);
                e17.setFc(nxq+1);
                semanticStack.push(e17);
                String temp13 = newTemp();
                generate("j==", iName1_17, iName2_17, String.valueOf(0));
                generate("j",String.valueOf(-1));
                break;
            case 18:
                SemanticNode i2_18 = semanticStack.pop();
                SemanticNode i1_18 = semanticStack.pop();
                String iName2_18 = i2_18.getPlace();
                String iName1_18 = i1_18.getPlace();

                SemanticNode e18 = new SemanticNode();
                e18.setTc(nxq);
                e18.setFc(nxq+1);
                semanticStack.push(e18);

                generate("j<", iName1_18, iName2_18, String.valueOf(0));
                generate("j",String.valueOf(-1));
                break;
            case 19:
                SemanticNode i2_19 = semanticStack.pop();
                SemanticNode i1_19 = semanticStack.pop();
                String iName2_19 = i2_19.getPlace();
                String iName1_19 = i1_19.getPlace();

                SemanticNode e19 = new SemanticNode();
                e19.setTc(nxq);
                e19.setFc(nxq+1);
                semanticStack.push(e19);

                generate("j>", iName1_19, iName2_19, String.valueOf(0));
                generate("j",String.valueOf(-1));
                break;
            case 20:
                SemanticNode i2_20 = semanticStack.pop();
                SemanticNode i1_20 = semanticStack.pop();
                String iName2_20 = i2_20.getPlace();
                String iName1_20 = i1_20.getPlace();

                SemanticNode e20 = new SemanticNode();
                e20.setTc(nxq);
                e20.setFc(nxq+1);
                semanticStack.push(e20);

                generate("j<=", iName1_20, iName2_20, String.valueOf(0));
                generate("j",String.valueOf(-1));
                break;
            case 21:
                SemanticNode i2_21 = semanticStack.pop();
                SemanticNode i1_21 = semanticStack.pop();
                String iName2_21 = i2_21.getPlace();
                String iName1_21 = i1_21.getPlace();

                SemanticNode e21 = new SemanticNode();
                e21.setTc(nxq);
                e21.setFc(nxq+1);
                semanticStack.push(e21);

                generate("j>=", iName1_21, iName2_21, String.valueOf(0));
                generate("j",String.valueOf(-1));
                break;

            case 23:
                SemanticNode sn23 = semanticStack.pop();
                Integer temp23= sn23.getTc();
                sn23.setTc(sn23.getFc());
                sn23.setFc(temp23);
                semanticStack.push(sn23);
                break;
            //Eand->E_12
            case 25:
                SemanticNode sn25 = semanticStack.pop();
                backPatch(sn25.getTc(), nxq);
                semanticStack.push(sn25);
                break;

            //EE->Eand_EE
            case 26:
                SemanticNode e26 = semanticStack.pop();
                SemanticNode eAnd26 = semanticStack.pop();
                SemanticNode sn26= new SemanticNode();
                sn26.setTc(e26.getTc());
                sn26.setFc(merge(eAnd26.getFc(), e26.getFc()));
                semanticStack.push(sn26);
                break;

                //Eor->E_13
            case 27:
                SemanticNode sn27 = semanticStack.pop();
                backPatch(sn27.getFc(), nxq);
                semanticStack.push(sn27);
                break;
            //EE->Eor_EE
            case 28:
                SemanticNode e28 = semanticStack.pop();
                SemanticNode eAnd28 = semanticStack.pop();
                SemanticNode sn28= new SemanticNode();
                sn28.setFc(e28.getFc());
                sn28.setTc(merge(eAnd28.getTc(), e28.getTc()));
                semanticStack.push(sn28);
                break;
            //control->if_25_statements_26
            case 29:
                SemanticNode s29 = semanticStack.pop();
                SemanticNode if29 = semanticStack.pop();
                SemanticNode sn29 = new SemanticNode();
                backPatch(if29.getChain(), s29.getChain());//����
                sn29.setChain(merge(if29.getChain(), s29.getChain()));
                semanticStack.push(sn29);
                break;
            //control->tp_25_statements_26
            case 30:
                SemanticNode s30 = semanticStack.pop();
                SemanticNode tp30 = semanticStack.pop();
                SemanticNode sn30 = new SemanticNode();
                sn30.setChain(merge(tp30.getChain(), s30.getChain()));
                semanticStack.push(sn30);
                break;
            //tp->if_25_statements_26_49
            case 31:
                SemanticNode s31 = semanticStack.pop();
                SemanticNode if31 = semanticStack.pop();
                SemanticNode tp31 = new SemanticNode();
                backPatch(if31.getChain(), nxq+1);
                tp31.setChain(merge(s31.getChain(), nxq));
                generate("j", String.valueOf(nxq+2));
                semanticStack.push(tp31);
                break;
            //if->55_23_EE_24
            case 32:
                SemanticNode ee32 = semanticStack.pop();
                backPatch(ee32.getTc(), nxq);
                SemanticNode sn32 = new SemanticNode();
                sn32.setChain(ee32.getFc());
                semanticStack.push(sn32);
                break;
            //control->while_25_statements_26
            case 33:
                SemanticNode s33 = semanticStack.pop();
                SemanticNode while33 = semanticStack.pop();
                SemanticNode control33 = new SemanticNode();
                backPatch(s33.getChain(), while33.getQuad());
                generate("j",while33.getQuad().toString());
                control33.setChain(while33.getChain());
                semanticStack.push(control33);
                break;
            //while->w_23_EE_24
            case 34:
                SemanticNode ee34 = semanticStack.pop();
                SemanticNode w34 = semanticStack.pop();
                SemanticNode while34 = new SemanticNode();
                backPatch(ee34.getTc(),nxq);
                while34.setChain(ee34.getFc());
                while34.setQuad(w34.getQuad());
                semanticStack.push(while34);
                break;
            //w->71
            case 35:
                SemanticNode w35 = new SemanticNode();
                w35.setQuad(nxq);
                semanticStack.push(w35);
                break;
            //control->72_23_printable_24_22
            case 37:
                SemanticNode printable = semanticStack.pop();
                String pName = printable.getPlace();
                generate("printf", pName, "_");
                SemanticNode sn37 = new SemanticNode();
                sn37.setChain(-1);
                semanticStack.push(sn37);
                break;
            //control->73_23_90_24_22
            case 38:
                SemanticNode id38 = semanticStack.pop();
                String iName38 = id38.getPlace();
                generate("scanf", iName38, "_");
                SemanticNode sn38 = new SemanticNode();
                sn38.setChain(-1);
                semanticStack.push(sn38);
                break;



            //statement->definition
            case 44:
                SemanticNode sn44 = semanticStack.pop();
                backPatch(sn44.getChain(), nxq);
                sn44.setChain(nxq);
                semanticStack.push(sn44);
                break;
            //statements->assign
            case 45:
                SemanticNode sn45 = semanticStack.pop();
                backPatch(sn45.getChain(), nxq);
                sn45.setChain(nxq);
                semanticStack.push(sn45);
                break;
           //statements->control
            case 46:
                SemanticNode sn46 = semanticStack.pop();
                backPatch(sn46.getChain(), nxq);
                sn46.setChain(nxq);
                semanticStack.push(sn46);
                break;
            //statements->44_definition
            case 47:
                SemanticNode sn47 = semanticStack.pop();
                String symbol47 = sn47.getPlace();
                symbolTable.get(symbol47).setKind(Kind.Constant);
                semanticStack.push(sn47);
                break;
            default:
        }
    }


    /**
     * ��ӡ�ķ�
     */
    public void printGrammar() {
        System.out.println("?????");
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
        System.out.println("�ս��");
        for (String c : vtSet) {
            System.out.println(c);
        }
    }

//    /**
//     * ????????
//     */
//    public void printItemLists() {
//        System.out.println("???????");
//        for (int i = 0; i < itemLists.size(); i++) {
//            System.out.println("I" + i + ":");
//            List<int[]> itemList = itemLists.get(i);
//            for (int[] item : itemList) {
//                int grammarNum = item[0];
//                int pos = item[1];
//                String[] grammar = grammars.get(grammarNum);
//                if(EMPTY.equals(grammar[1])){
//                    System.out.println("\t" + grammar[0] + LINK + "??");
//                } else {
//                    System.out.println("\t" + grammar[0] + LINK
//                            + grammar[1].substring(0, pos) + "??"
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
        System.out.println("go??");
        System.out.println();
        int count = 0;
        for (Map<String, Integer> goItem : goTable) {
            System.out.print("I" + count + " ");
            System.out.println(goItem.toString());
            count++;
        }

    }

    /**
     * ��ӡ��Ԫʽ
     */
    public void printFourElementList(){
        System.out.println("******************��Ԫʽ********************");
        for(int i=0; i<fourElementList.size(); i++){
            FourElement e = fourElementList.get(i);
            System.out.println(i+":  ("+e.getOp()+", "+e.getArg1()+", "+e.getArg2()+", "+e.getResult()+")");
        }
    }

    /**
     * ��ӡ���ű�
     */
    public void printSymbolTable(){
        System.out.println("******************���ű�********************");
        System.out.printf("%.8s  %.8s  %.8s\n", "������","����","����");
        for(String name : symbolTable.keySet()){
            Info info = symbolTable.get(name);
            System.out.printf("%.8s  %.8s  %.8s\n", name,info.getKind().name(),info.getTypeId().toString());

        }
    }


}
