datasg segment
    a dw 0
    1 dw 0
    T1 dw 0
    10 dw 0
    printf_a5 db 'a:$'
datasg ends

codesg segment
assume cs:codesg,ds:datasg
start:
    mov AX,datasg
    mov DS,AX
L0: mov AX, 1
    mov a, AX
L1: cmp a, 10
    jl 3
L2: jmp L5
L3: mov AX, a
    add AX, 1
    mov T1, AX
L4: mov AX, T1
    mov a, AX


;printf
L5:
    lea dx,printf_a5
    mov ah,9
    int 21h
    mov ax,a
    xor cx,cx
    mov bx,10
    PT05:xor dx,dx
    div bx
    or dx,0e30h;0e:��ʾ�ַ�
    push dx
    inc cx
    cmp ax,0;ZF=1��AX=0,ZF=0��AX��=0
    jnz PT05;���ʱ��ת
    PT15:pop ax
    int 10h;��ʾһ���ַ�
    loop PT15
    mov ah,0 
    ;int 16h ;�����ж�
    ;����
     mov dl,0dh
     mov ah,2
     int 21h
     mov dl,0ah
     mov ah,2
     int 21h


    mov ax,4c00h; int 21h��4ch���жϣ���ȫ�˳�����
    int 21h;����ϵͳ�ж�
codesg ends
end start
