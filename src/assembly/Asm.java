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
 *  ������������
 */
public class Asm {
	
	private List<String> asmList; //������
	private List<FourElement> fourElemList; //��Ԫʽ
	private Map<String, Info> symbolTable; //���ű�
	/**
	 * @param fourElemList //��Ԫʽ
	 */
	public Asm(List<FourElement> fourElemList,Map<String, Info> symbolTable) {
	    asmList=new ArrayList<String>();
		this.fourElemList=fourElemList;
		this.symbolTable = symbolTable;
		addDataSegment();//���ͷ��
		addCodeSegment();//���ɴ���δ���
		//asmTail();//���β��

	}





	/**
	 * ���ͷ��
	 */
	public void addDataSegment() {

		//������ݶδ���
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
	 *  ���ɴ���δ���
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
			    asmList.add("jnz L" + fourElement.getArg1()+";����������ת");
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
				//��ʾ��ӡ��ʾ
				asmList.add("    lea dx,printf_"+fourElement.getArg1()+(i));
				asmList.add("    mov ah,9"); //9�Ź�������
				asmList.add("    int 21h");

				asmList.add("    mov ax,"+fourElement.getArg1());//��ȡ��Ҫ��ӡ����ֵ
				// ��cx����
				asmList.add("    xor cx,cx");
				asmList.add("    mov bx,10");

				asmList.add("    PT0"+(i)+":xor dx,dx");
				// ����10����ȡ����λ���ϵ�ֵ
				/*
				 * ����������Ϊ8λ����������Ϊ16λ��AL���̣�AH����
				 * ����λ16λ����������Ϊ32λ��AX���̣�DX����
				 */
				asmList.add("    div bx");
				/*
                 * AHΪ0EH��ALΪ��ʾ�ַ���ASCLL������ʾ�ַ�
                 */
				asmList.add("    or dx,0e30h;0e:��ʾ�ַ�");
				//����ʾ���ַ�����ջ��
				asmList.add("    push dx");
				//��ʾ���ַ�����
				asmList.add("    inc cx");
				//cmp��������ֻ�ı��־λ�����������Ŀ�Ĳ�����
				asmList.add("    cmp ax,0;ZF=1��AX=0,ZF=0��AX��=0");
				asmList.add("    jnz PT0"+(i)+";���ʱ��ת");
				//����Ҫ��ʾ���ַ����δ�ջ����ʾ����
				asmList.add("    PT1"+(i)+":pop ax");
				asmList.add("    int 10h;��ʾһ���ַ�");
				asmList.add("    loop PT1"+(i));
				asmList.add("    mov ah,0 ");
				asmList.add("    ;int 16h ;�����ж�");

				asmList.add("    ;����");
				asmList.add("     mov dl,0dh");
				asmList.add("     mov ah,2");
				asmList.add("     int 21h");
				asmList.add("     mov dl,0ah");
				asmList.add("     mov ah,2");
				asmList.add("     int 21h");
				asmList.add("\n");

			}else if (fourElement.getOp().equals("scanf")) {  // ���봦��
				asmList.add("L" + (i) + ":");

				asmList.add("\n");
				asmList.add("    ;scanf");
				//��ʾ������ʾ


				asmList.add("    lea dx,scanf_"+fourElement.getArg1()+(i));
				asmList.add("    mov ah,9");
				asmList.add("    int 21h");

				//����ñ���ֵ
				asmList.add("   ;�����ж�");
				asmList.add("    mov al,0h;");
				asmList.add("    mov tem[1],al;");
				/*
                    �����ַ���������
                    �������ĵ�һ���ֽ�ָ�����ɵ�����ַ����������û�����
                    �ڶ����ֽڴ��ʵ�ʵ�����ַ���������ϵͳ�������
                    �ӵ������ֽڿ�ʼ��ŴӼ��̽��ܵ��ַ�,ֱ��ENTER������
                 */
				asmList.add("    lea dx,tem;");
				asmList.add("    mov ah,0ah");
				asmList.add("    int 21h");
				asmList.add("    ;������������ݣ�����ֵ������");
				// ʹ��ѭ�����������е����ݴ浽������
				// ѭ����������ʼ��
				asmList.add("    mov cl,0000h;");
				// ʵ��������ַ�����
				asmList.add("    mov al,tem[1];");
				asmList.add("    sub al,1;");
				// ѭ����������
				asmList.add("    mov cl,al;");


				asmList.add("    mov ax,0000h;");
				asmList.add("    mov bx,0000h;");

				asmList.add("    mov al,tem[2];");
				// ASCII���30λ���ַ���ʾ����ֵ
				asmList.add("    sub al,30h;");
				asmList.add("    mov "+fourElement.getArg1()+",ax;");//����Ӧ������ֵ

				// ���ֻ������һ����ֵ��ֱ�����������βλ��
				asmList.add("    mov ax,cx");
				asmList.add("    sub ax,1");
				asmList.add("    jc inputEnd"+(i)+";С������ת");

				// ����ƫ�Ƶ�ַ����tem�ĵ���λ��ʼ����һλ���ַ��������ڶ�λǰ���Ѿ���ֵ��
				asmList.add("    ;");
				asmList.add("    MOV SI,0003H;");

				// �����˶���ַ����͸�ǰ������е�ֵ*10
				asmList.add("    ln"+(i)+":mov bx,10;");
				asmList.add("    mov ax,"+fourElement.getArg1()+";");

				asmList.add("    mul bx;");
				asmList.add("    mov "+fourElement.getArg1()+",ax;");
				//�����������ϵ���һ��ֵ
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

				asmList.add("    ;����");
				asmList.add("    mov dl,0dh");
				asmList.add("    mov ah,2");
				asmList.add("    int 21h");
				asmList.add("    mov dl,0ah");
				asmList.add("    mov ah,2");
				asmList.add("    int 21h");
				asmList.add("\n");


			}

		}


		asmList.add("L"+fourElemList.size()+": mov ax,4c00h; int 21h��4ch���жϣ���ȫ�˳�����");
		asmList.add("    int 21h;����ϵͳ�ж�");
		asmList.add("codesg ends");
		asmList.add("end start");
		
		
		
		
	}



	
	
//
//	/**
//	 * ���β��
//	 */
//	public void asmTail() {
//
//	}


	public void printtAsm(){
	    System.out.println("**************������**************");
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
