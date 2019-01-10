package assembly;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import syntax.FourElement;
import syntax.Info;
import syntax.Kind;

/**
 *  
 * 
 *  汇编代码生成类
 */
public class Asm {
	
	private List<String> asmList; //汇编代码
	private List<FourElement> fourElemList; //四元式
	private Map<String, Info> symbolTable; //符号表
	/**
	 * @param fourElemList //四元式
	 */
	public Asm(List<FourElement> fourElemList,Map<String, Info> symbolTable) {
	    asmList=new ArrayList<String>();
		this.fourElemList=fourElemList;
		this.symbolTable = symbolTable;
		addDataSegment();//汇编头部
		addCodeSegment();//生成代码段代码
		//asmTail();//汇编尾部

	}





	/**
	 * 汇编头部
	 */
	public void addDataSegment() {

		//添加数据段代码
		asmList.add("datasg segment");
		asmList.add("tem db 6,7 dup  (0)");
		for (String name : symbolTable.keySet()) {
			Info info = symbolTable.get(name);
			if(info.getKind().equals(Kind.Constant)) {
				char[] nameChar = name.toCharArray();
				boolean isNum = true;
				for(char c : nameChar){
					if(!Character.isDigit(c)){
						isNum = false;
					}
				}
				if(isNum){
					continue;
				}
			}
			asmList.add("    " + name + " dw 0");
		}

		for (int i = 0; i < fourElemList.size(); i++) {
			if(fourElemList.get(i).getOp().equals("printf")){

				asmList.add("    printf_"+fourElemList.get(i).getArg1()+(i)+" db '"+fourElemList.get(i).getArg1()+":$'");

			}else if(fourElemList.get(i).getOp().equals("scanf")){
				asmList.add("    scanf_"+fourElemList.get(i).getArg1()+(i)+" db 'input "+fourElemList.get(i).getArg1()+":$'");
			}
		}


		asmList.add("datasg ends");

		asmList.add("");


	}



	/**
	 *  生成代码段代码
	 * @param
	 */
	public void addCodeSegment() {
		asmList.add("codesg segment");
		asmList.add("assume cs:codesg,ds:datasg");
		asmList.add("start:");
		asmList.add("    mov AX,datasg");
		asmList.add("    mov DS,AX");

		for (int i = 0; i < fourElemList.size(); i++) {

		    FourElement fourElement = fourElemList.get(i);

			if (fourElement.getOp().equals("=")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("+")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    add AX, " + fourElement.getArg2());
				asmList.add("    mov " + fourElement.getResult() + ", AX");
			}else if (fourElement.getOp().equals("++")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    add AX, 1");
				asmList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("-")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    sub AX, " + fourElement.getArg2());
				asmList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("*")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    mov BX," + fourElement.getArg2());
				asmList.add("    mul BX");
				asmList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("/")) {

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    mov BX," + fourElement.getArg2());
				asmList.add("    div BL");
				asmList.add("    mov ah,0h");
				asmList.add("    mov " + fourElement.getResult() + ", Ax");
			}
			else if (fourElement.getOp().equals("jnz")) {
                asmList.add("L" + (i) + ": cmp "+fourElement.getArg1());
			    asmList.add("jnz L" + fourElement.getArg1()+";不等于则跳转");
			} else if (fourElement.getOp().equals("j")) {
					asmList.add("L" + (i) + ": jmp L" + fourElement.getResult());
			} else if (  fourElement.getOp().equals("<")) {
				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    sub AX, " + fourElement.getArg2());
			}else if (fourElement.getOp().equals(">") ){

				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				asmList.add("    sub AX, " + fourElement.getArg1());

			}else if (fourElement.getOp().equals(">=") ){

				//asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				//asmList.add("sub AX, " + fourElement.getArg1());


				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmList.add("    add AX, 1");
				asmList.add( "    mov BX ,AX");
				asmList.add("     mov AX, " + fourElement.getArg2());
				asmList.add("    sub AX, BX");


			}else if (fourElement.getOp().equals("<=") ){
				asmList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				asmList.add("    add AX, 1");
				asmList.add( "    mov BX ,AX");
				asmList.add("    mov AX, " + fourElement.getArg1());
				asmList.add("    sub AX, BX");

			} else if (  fourElement.getOp().equals("j<")) {
                asmList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmList.add("    jl "+fourElement.getResult());
            }else if (fourElement.getOp().equals("j>") ){
                asmList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmList.add("    jg "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j<=") ){
                asmList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmList.add("    jle "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j>=") ){
                asmList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmList.add("    jge "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j==") ){
                asmList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmList.add("    je "+fourElement.getResult());
            }

			else if (fourElement.getOp().equals("printf")) {
				asmList.add("\n");
				asmList.add(";printf");
				asmList.add("L" + (i) + ":");
				//显示打印提示
				asmList.add("    lea dx,printf_"+fourElement.getArg1()+(i));
				asmList.add("    mov ah,9"); //9号功能输入
				asmList.add("    int 21h");

				asmList.add("    mov ax,"+fourElement.getArg1());//获取需要打印的数值
				// 把cx置零
				asmList.add("    xor cx,cx");
				asmList.add("    mov bx,10");

				asmList.add("    PT0"+(i)+":xor dx,dx");
				// 除以10，获取各个位数上的值
				/*
				 * 除法：除数为8位，被除数则为16位，AL存商，AH存余
				 * 除数位16位，被除数则为32位，AX存商，DX存余
				 */
				asmList.add("    div bx");
				/*
                 * AH为0EH，AL为显示字符的ASCLL才能显示字符
                 */
				asmList.add("    or dx,0e30h;0e:显示字符");
				//将显示的字符放入栈中
				asmList.add("    push dx");
				//显示的字符个数
				asmList.add("    inc cx");
				//cmp作减法，只改变标志位，结果不存入目的操作数
				asmList.add("    cmp ax,0;ZF=1则AX=0,ZF=0则AX！=0");
				asmList.add("    jnz PT0"+(i)+";相等时跳转");
				//将需要显示的字符依次从栈中显示出来
				asmList.add("    PT1"+(i)+":pop ax");
				asmList.add("    int 10h;显示一个字符");
				asmList.add("    loop PT1"+(i));
				asmList.add("    mov ah,0 ");
				asmList.add("    ;int 16h ;键盘中断");

				asmList.add("    ;换行");
				asmList.add("     mov dl,0dh");
				asmList.add("     mov ah,2");
				asmList.add("     int 21h");
				asmList.add("     mov dl,0ah");
				asmList.add("     mov ah,2");
				asmList.add("     int 21h");
				asmList.add("\n");

			}else if (fourElement.getOp().equals("scanf")) {  // 输入处理
				asmList.add("L" + (i) + ":");

				asmList.add("\n");
				asmList.add("    ;scanf");
				//显示输入提示


				asmList.add("    lea dx,scanf_"+fourElement.getArg1()+(i));
				asmList.add("    mov ah,9");
				asmList.add("    int 21h");

				//输入该变量值
				asmList.add("   ;输入中断");
				asmList.add("    mov al,0h;");
				asmList.add("    mov tem[1],al;");
				/*
                    输入字符到缓冲区
                    缓冲区的第一个字节指定容纳的最大字符个数，由用户给出
                    第二个字节存放实际的最大字符个数，由系统最后添入
                    从第三个字节开始存放从键盘接受的字符,直到ENTER键结束
                 */
				asmList.add("    lea dx,tem;");
				asmList.add("    mov ah,0ah");
				asmList.add("    int 21h");
				asmList.add("    ;处理输入的数据，并赋值给变量");
				// 使用循环将缓冲区中的数据存到变量中
				// 循环计数器初始化
				asmList.add("    mov cl,0000h;");
				// 实际输入的字符个数
				asmList.add("    mov al,tem[1];");
				asmList.add("    sub al,1;");
				// 循环次数设置
				asmList.add("    mov cl,al;");


				asmList.add("    mov ax,0000h;");
				asmList.add("    mov bx,0000h;");

				asmList.add("    mov al,tem[2];");
				// ASCII码减30位该字符表示的数值
				asmList.add("    sub al,30h;");
				asmList.add("    mov "+fourElement.getArg1()+",ax;");//给对应变量赋值

				// 如果只输入了一个数值就直接跳到输入结尾位置
				asmList.add("    mov ax,cx");
				asmList.add("    sub ax,1");
				asmList.add("    jc inputEnd"+(i)+";小于则跳转");

				// 设置偏移地址，从tem的第三位开始（第一位是字符个数，第二位前面已经赋值）
				asmList.add("    ;");
				asmList.add("    MOV SI,0003H;");

				// 输入了多个字符，就给前面变量中的值*10
				asmList.add("    ln"+(i)+":mov bx,10;");
				asmList.add("    mov ax,"+fourElement.getArg1()+";");

				asmList.add("    mul bx;");
				asmList.add("    mov "+fourElement.getArg1()+",ax;");
				//给变量赋加上的下一个值
				asmList.add("    mov ax,0000h;");
				asmList.add("    mov al,tem[si]");
				asmList.add("    sub al,30h;");
				asmList.add("    add ax,"+fourElement.getArg1()+";");
				asmList.add("    mov "+fourElement.getArg1()+",ax");
				asmList.add("    INC SI");
				asmList.add("    loop ln"+(i));
				asmList.add("    inputEnd"+(i)+": nop");
				asmList.add("");
				asmList.add("");

				asmList.add("    ;换行");
				asmList.add("    mov dl,0dh");
				asmList.add("    mov ah,2");
				asmList.add("    int 21h");
				asmList.add("    mov dl,0ah");
				asmList.add("    mov ah,2");
				asmList.add("    int 21h");
				asmList.add("\n");


			}

		}


		asmList.add("L"+fourElemList.size()+": mov ax,4c00h; int 21h的4ch号中断，安全退出程序。");
		asmList.add("    int 21h;调用系统中断");
		asmList.add("codesg ends");
		asmList.add("end start");
		
		
		
		
	}



	
	
//
//	/**
//	 * 汇编尾部
//	 */
//	public void asmTail() {
//
//	}


	public void printtAsm(){
	    System.out.println("**************汇编代码**************");
	    for(String addCodeSegment:asmList){
	        System.out.println(addCodeSegment);
        }
    }

    public void writeAsmToFile(String filePath, String fileName){
        try {
            File outputFile = new File(filePath, fileName);
            if(!outputFile.exists()){
                outputFile.createNewFile();
            }
            PrintWriter pr = new PrintWriter(outputFile.getPath());
            for(String addCodeSegment : asmList) {
                pr.println(addCodeSegment);
            }
            pr.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not exist");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("io exception");
            e.printStackTrace();
        }
    }
	
	
	
}
