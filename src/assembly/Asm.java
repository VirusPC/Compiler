package assembly;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import syntax.FourElement;
import syntax.Info;

/**
 *  
 * 
 *  ������������
 */
public class Asm {
	
	private List<String> asmCodeList;
	private List<FourElement> fourElemList;
	private Map<String, Info> symbolTable;
	/**
	 * @param fourElemList //��Ԫʽ
	 */
	public Asm(List<FourElement> fourElemList,Map<String, Info> symbolTable) {
	    asmCodeList=new ArrayList<String>();
		this.fourElemList=fourElemList;
		this.symbolTable = symbolTable;
		asmHead();//���ͷ��
		asmCode();//���ɴ���δ���
		asmTail();//���β��

	}
	
	
//
//	/**
//	 * ��ȡasm�ļ���ַ
//	 * @return
//	 * @throws IOException
//	 */
//	public String getAsmFile() throws IOException {
//
//		File file = new File("./result/");
//		if (!file.exists()) {
//			file.mkdirs();
//			file.createNewFile();// �������ļ������ھʹ�����
//		}
//		String path = file.getAbsolutePath();
//		FileOutputStream fos = new FileOutputStream(path + "/c_to_asm.asm");
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		OutputStreamWriter osw1 = new OutputStreamWriter(bos, "gbk");
//		PrintWriter pw1 = new PrintWriter(osw1);
//
//		for(int i=0;i<asmCodeList.size();i++)
//			pw1.println(asmCodeList.get(i));
//
//		pw1.close();
//		return path + "/c_to_asm.asm";
//
//	}




	/**
	 * ���ͷ��
	 */
	public void asmHead() {

		//������ݶδ���
		asmCodeList.add("datasg segment");
		for (String name : symbolTable.keySet()) {
			asmCodeList.add("    "+name + " dw 0");
		}

		for (int i = 0; i < fourElemList.size(); i++) {
			if(fourElemList.get(i).getOp().equals("printf")){

				asmCodeList.add("    printf_"+fourElemList.get(i).getArg1()+(i)+" db '"+fourElemList.get(i).getArg1()+":$'");

			}else if(fourElemList.get(i).getOp().equals("scanf")){
				asmCodeList.add("    scanf_"+fourElemList.get(i).getArg1()+(i)+" db 'input "+fourElemList.get(i).getArg1()+":$'");
			}
		}


			asmCodeList.add("datasg ends");
			asmCodeList.add("");
			asmCodeList.add("codesg segment");
			asmCodeList.add("assume cs:codesg,ds:datasg");
			asmCodeList.add("start:");
			asmCodeList.add("    mov AX,datasg");
			asmCodeList.add("    mov DS,AX");


	}



	/**
	 *  ���ɴ���δ���
	 * @param
	 */
	public void asmCode() {

		for (int i = 0; i < fourElemList.size(); i++) {

		    FourElement fourElement = fourElemList.get(i);

			if (fourElement.getOp().equals("=")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("+")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    add AX, " + fourElement.getArg2());
				asmCodeList.add("    mov " + fourElement.getResult() + ", AX");
			}else if (fourElement.getOp().equals("++")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    add AX, 1");
				asmCodeList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("-")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    sub AX, " + fourElement.getArg2());
				asmCodeList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("*")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    mov BX," + fourElement.getArg2());
				asmCodeList.add("    mul BX");
				asmCodeList.add("    mov " + fourElement.getResult() + ", AX");
			} else if (fourElement.getOp().equals("/")) {

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    mov BX," + fourElement.getArg2());
				asmCodeList.add("    div BL");
				asmCodeList.add("    mov ah,0h");
				asmCodeList.add("    mov " + fourElement.getResult() + ", Ax");
			}
			else if (fourElement.getOp().equals("jnz")) {
                asmCodeList.add("L" + (i) + ": cmp "+fourElement.getArg1());
			    asmCodeList.add("jnz L" + fourElement.getArg1()+";����������ת");
			} else if (fourElement.getOp().equals("j")) {
					asmCodeList.add("L" + (i) + ": jmp L" + fourElement.getResult());
			} else if (  fourElement.getOp().equals("<")) {
				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    sub AX, " + fourElement.getArg2());
			}else if (fourElement.getOp().equals(">") ){

				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				asmCodeList.add("    sub AX, " + fourElement.getArg1());

			}else if (fourElement.getOp().equals(">=") ){

				//asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				//asmCodeList.add("sub AX, " + fourElement.getArg1());


				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg1());
				asmCodeList.add("    add AX, 1");
				asmCodeList.add( "    mov BX ,AX");
				asmCodeList.add("     mov AX, " + fourElement.getArg2());
				asmCodeList.add("    sub AX, BX");


			}else if (fourElement.getOp().equals("<=") ){
				asmCodeList.add("L" + (i) + ": mov AX, " + fourElement.getArg2());
				asmCodeList.add("    add AX, 1");
				asmCodeList.add( "    mov BX ,AX");
				asmCodeList.add("    mov AX, " + fourElement.getArg1());
				asmCodeList.add("    sub AX, BX");

			} else if (  fourElement.getOp().equals("j<")) {
                asmCodeList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmCodeList.add("    jl "+fourElement.getResult());
            }else if (fourElement.getOp().equals("j>") ){
                asmCodeList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmCodeList.add("    jg "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j<=") ){
                asmCodeList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmCodeList.add("    jle "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j>=") ){
                asmCodeList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmCodeList.add("    jge "+fourElement.getResult());

            }else if (fourElement.getOp().equals("j==") ){
                asmCodeList.add("L" + (i) + ": cmp " + fourElement.getArg1()+", "+ fourElement.getArg2());
                asmCodeList.add("    je "+fourElement.getResult());
            }

			else if (fourElement.getOp().equals("printf")) {
				asmCodeList.add("\n");
				asmCodeList.add(";printf");
				asmCodeList.add("L" + (i) + ":");
				asmCodeList.add("    lea dx,printf_"+fourElement.getArg1()+(i));
				asmCodeList.add("    mov ah,9"); //9�Ź�������
				asmCodeList.add("    int 21h");

				asmCodeList.add("    mov ax,"+fourElement.getArg1());
				asmCodeList.add("    xor cx,cx");
				asmCodeList.add("    mov bx,10");
				asmCodeList.add("    PT0"+(i)+":xor dx,dx");
				asmCodeList.add("    div bx");
				asmCodeList.add("    or dx,0e30h;0e:��ʾ�ַ�");
				asmCodeList.add("    push dx");
				asmCodeList.add("    inc cx");
				asmCodeList.add("    cmp ax,0;ZF=1��AX=0,ZF=0��AX��=0");
				asmCodeList.add("    jnz PT0"+(i)+";���ʱ��ת");
				asmCodeList.add("    PT1"+(i)+":pop ax");
				asmCodeList.add("    int 10h;��ʾһ���ַ�");
				asmCodeList.add("    loop PT1"+(i));
				asmCodeList.add("    mov ah,0 ");
				asmCodeList.add("    ;int 16h ;�����ж�");

				asmCodeList.add("    ;����");
				asmCodeList.add("     mov dl,0dh");
				asmCodeList.add("     mov ah,2");
				asmCodeList.add("     int 21h");
				asmCodeList.add("     mov dl,0ah");
				asmCodeList.add("     mov ah,2");
				asmCodeList.add("     int 21h");
				asmCodeList.add("\n");

			}else if (fourElement.getOp().equals("scanf")) {
				asmCodeList.add("L" + (i) + ":");

				asmCodeList.add("\n");
				asmCodeList.add("    ;scanf");



				asmCodeList.add("    lea dx,scanf_"+fourElement.getArg1()+(i));
				asmCodeList.add("    mov ah,9");
				asmCodeList.add("    int 21h");

				asmCodeList.add("   ;�����ж�");
				asmCodeList.add("    mov al,0h;");
				asmCodeList.add("    mov tem[1],al;");
				asmCodeList.add("    lea dx,tem;");
				asmCodeList.add("    mov ah,0ah");
				asmCodeList.add("    int 21h");
				asmCodeList.add("    ;������������ݣ�����ֵ������");
				asmCodeList.add("    mov cl,0000h;");
				asmCodeList.add("    mov al,tem[1];");
				asmCodeList.add("    sub al,1;");
				asmCodeList.add("    mov cl,al;");

				asmCodeList.add("    mov ax,0000h;");
				asmCodeList.add("    mov bx,0000h;");

				asmCodeList.add("    mov al,tem[2];");
				asmCodeList.add("    sub al,30h;");
				asmCodeList.add("    mov "+fourElement.getArg1()+",ax;");


				asmCodeList.add("    mov ax,cx");
				asmCodeList.add("    sub ax,1");
				asmCodeList.add("    jc inputEnd"+(i)+";С������ת");

				asmCodeList.add("    ;");
				asmCodeList.add("    MOV SI,0003H;");


				asmCodeList.add("    ln"+(i)+":mov bx,10;");
				asmCodeList.add("    mov ax,"+fourElement.getArg1()+";");

				asmCodeList.add("    mul bx;");
				asmCodeList.add("    mov "+fourElement.getArg1()+",ax;");
				asmCodeList.add("    mov ax,0000h;");
				asmCodeList.add("    mov al,tem[si]");
				asmCodeList.add("    sub al,30h;");
				asmCodeList.add("    add ax,"+fourElement.getArg1()+";");
				asmCodeList.add("    mov "+fourElement.getArg1()+",ax");
				asmCodeList.add("    INC SI");
				asmCodeList.add("    loop ln"+(i));
				asmCodeList.add("    inputEnd"+(i)+": nop");
				asmCodeList.add("");
				asmCodeList.add("");

				asmCodeList.add("    ;����");
				asmCodeList.add("    mov dl,0dh");
				asmCodeList.add("    mov ah,2");
				asmCodeList.add("    int 21h");
				asmCodeList.add("    mov dl,0ah");
				asmCodeList.add("    mov ah,2");
				asmCodeList.add("    int 21h");
				asmCodeList.add("\n");


			}

		}
		
		
		
		
	}



	
	
	
	/**
	 * ���β��
	 */
	public void asmTail() {

		
		asmCodeList.add("    mov ax,4c00h; int 21h��4ch���жϣ���ȫ�˳�����");
		asmCodeList.add("    int 21h;����ϵͳ�ж�");
		asmCodeList.add("codesg ends");
		asmCodeList.add("end start");

	}


	public void printtAsm(){
	    System.out.println("**************������**************");
	    for(String asmCode:asmCodeList){
	        System.out.println(asmCode);
        }
    }

    public void writeAsmToFile(String filePath, String fileName){
        try {
            File outputFile = new File(filePath, fileName);
            if(!outputFile.exists()){
                outputFile.createNewFile();
            }
            PrintWriter pr = new PrintWriter(outputFile.getPath());
            for(String asmCode : asmCodeList) {
                pr.println(asmCode);
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
