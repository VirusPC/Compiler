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
    or dx,0e30h;0e:显示字符
    push dx
    inc cx
    cmp ax,0;ZF=1则AX=0,ZF=0则AX！=0
    jnz PT05;相等时跳转
    PT15:pop ax
    int 10h;显示一个字符
    loop PT15
    mov ah,0 
    ;int 16h ;键盘中断
    ;换行
     mov dl,0dh
     mov ah,2
     int 21h
     mov dl,0ah
     mov ah,2
     int 21h


    mov ax,4c00h; int 21h的4ch号中断，安全退出程序。
    int 21h;调用系统中断
codesg ends
end start
