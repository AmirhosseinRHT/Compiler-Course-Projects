.class public UTL
.super java/lang/Object
.field public balance Ljava/lang/Integer;
.field public tick_counts Ljava/lang/Integer;
.method public <init>()V
.limit stack 128
.limit locals 128
		aload_0
		invokespecial java/lang/Object/<init>()V
		areturn
.end method
.method public onInit(LTrade;)V
.limit stack 128
.limit locals 128
		
		new Order
		dup null
		ldc 100
		ldc 100
		ldc 10
		invokespecial Order/<init>(Ljava/lang/String;III)V
		astore_1
.end method
.method public onInit(LTrade;)V
.limit stack 128
.limit locals 128
		
		new Order
		dup null
		ldc 200
		ldc 50
		ldc 5
		invokespecial Order/<init>(Ljava/lang/String;III)V
		astore_1
		
		new Order
		dup null
		ldc 100
		ldc 100
		ldc 10
		invokespecial Order/<init>(Ljava/lang/String;III)V
		astore_2
.end method
.method public onStart(LTrade;)V
.limit stack 128
.limit locals 128
		
		ldc 100
		invokestatic java/lang/Float/valueOf(Z)Ljava/lang/Float;
		astore_1
		
		ldc 250
		invokestatic java/lang/Float/valueOf(Z)Ljava/lang/Float;
		astore_2
		
		ldc 20
		invokestatic java/lang/Float/valueOf(Z)Ljava/lang/Float;
		astore_3
		
		new Order
		dup null
		aload_1
		aload_2
		aload_3
		invokespecial Order/<init>(Ljava/lang/String;III)V
		astore 4
		
		aload_2
		
		aload_3
		idiv
		invokestatic java/lang/Float/valueOf(Z)Ljava/lang/Float;
		astore 5
.end method
.method public onStart(LTrade;)V
.limit stack 128
.limit locals 128
		
		ldc 100
		invokestatic Prog/getCandle(Ljava/lang/Integer)LCandle
.end method
.method public static main([Ljava/lang/String;)V
.limit stack 128
.limit locals 128
		new UTL
		invokespecial UTL/<init>()V
		astore_0
		ldc "admin"
		astore_1
		
		aload_1ldc "password"
		invokestatic Prog/Connect(Ljava/lang/String;Ljava/lang/String;)V
		ldc "USDETH"
		invokestatic Prog/Observe(Ljava/lang/Integer;)Ltrade
		astore_2
		ldc "IRRETH"
		invokestatic Prog/Observe(Ljava/lang/Integer;)Ltrade
		astore_3
		return
.end method
