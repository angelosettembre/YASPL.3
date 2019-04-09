#include<stdio.h>
#include<stdbool.h>

double sommagrande, sommapiccola;
int i, scelta;
double x, y, risultato;
bool grande, continua;
int scelta_cont;
int risFibonacci;

void moltiplicazione(double x, double y, double* res, bool* grande){
	double risultato = 0;
	int index = 0;
	while(index < y) {
		index = index + 1;
		risultato = risultato + x;
		
	}
	if(x * y >= 100) {
	*grande = true;
		
	} else {
		*grande = false;
		
	}
	*res = risultato;

}

void somma(double x, double y, double* res, bool* grande){
	double risultato = x + y;
	if(x + y >= 100) {
	*grande = true;
		
	} else {
		*grande = false;
		
	}
	*res = risultato;

}

void sottrazione(double x, double y, double* res, bool* grande){
	double risultato = x - y;
	if(x - y >= 100) {
	*grande = true;
		
	} else {
		*grande = false;
		
	}
	*res = risultato;

}

void divisione(double x, double y, double* res, bool* grande){
	double risultato = x / y;
	if(x / y >= 100) {
	*grande = true;
		
	} else {
		*grande = false;
		
	}
	*res = risultato;

}

void potenza(double x, double y, double* res){
	double risultato = 1;
	int index = 0;
	while(index < y) {
		risultato = risultato * x;
		index = index + 1;
		
	}
	*res = risultato;

}

void fibonacci(int n, int* ris){
	int f1 = 1;
	int f0 = 0;
	int fn = n;
	int i = 2;
	while(i <= n) {
		fn = f1 + f0;
		f0 = f1;
		f1 = fn;
		i = i + 1;
		
	}
	*ris = fn;

}

int main() {
	sommagrande = 0;
	sommapiccola = 0;
	printf("Questo programma CALCOLATRICE permette di svolgere una serie di addizioni (1), sottrazioni (2), moltiplicazioni (3), divisioni (4), elevamento a potenza (5), successione di fibonacci (6) ");	

	printf("Accumulando i risultati > 100 in sommagrande e quelli < 100 in sommapiccola ");	

	continua = false;
	while(!(continua)) {
		printf("Scegli una delle 6 operazioni (1), (2), (3), (4), (5), (6) ");	

		scanf("%d", &scelta);	

		if(scelta == 1 || scelta == 2 || scelta == 3 || scelta == 4 || scelta == 5 || scelta == 6) {
	if(scelta == 1) {
		printf("Addizione : inserisci due numeri positivi ");	

		scanf("%lf %lf", &x, &y);	

		somma(x,y,&risultato,&grande);
		printf("Risultato somma:  %lf", risultato);	

		
	}
		if(scelta == 2) {
		printf("Sottrazione : inserisci due numeri positivi ");	

		scanf("%lf %lf", &x, &y);	

		sottrazione(x,y,&risultato,&grande);
		printf("Risultato sottrazione:  %lf", risultato);	

		
	}
		if(scelta == 3) {
		printf("Moltiplicazione : inserisci due numeri positivi ");	

		scanf("%lf %lf", &x, &y);	

		moltiplicazione(x,y,&risultato,&grande);
		printf("Risultato moltiplicazione:  %lf", risultato);	

		
	}
		if(scelta == 4) {
		printf("Divisione : inserisci due numeri positivi ");	

		scanf("%lf %lf", &x, &y);	

		divisione(x,y,&risultato,&grande);
		printf("Risultato divisione:  %lf", risultato);	

		
	}
		if(scelta == 5) {
		printf("Elevamento a potenza : inserisci due numeri positivi ");	

		scanf("%lf %lf", &x, &y);	

		potenza(x,y,&risultato);
		printf("Risultato potenza :  %lf", risultato);	

		
	}
		if(scelta == 6) {
		printf("Successione di Fibonacci: inserisci un numero ");	

		scanf("%lf", &x);	

		i = 0;
		while(i <= x) {
		fibonacci(i,&risFibonacci);
		printf("Risultato successione di Fibonacci :  %d", risFibonacci);	

		i = i + 1;
		
	}
		
	}
		
	} else {
		continua = true;
		
	}
		if(!(continua)) {
		if(grande) {
	sommagrande = sommagrande + risultato;
		
	} else {
		sommapiccola = sommapiccola + risultato;
		
	}
		printf("Vuoi continuare? si --> (0) no --> (1) ");	

		scanf("%d", &scelta_cont);	

		if(scelta_cont == 1) {
	continua = true;
		
	} else {
		if(scelta_cont == 0) {
	continua = false;
		scelta = 0;
		
	} else {
		continua = true;
		
	}
		
	}
		
	}
		
	}
	printf("Valore di sommagrande:  %lf  ", sommagrande);	

	printf("Valore di sommapiccola:  %lf  ", sommapiccola);	

	printf("ciao alla prossima");	

	printf("! \n ");	

}
