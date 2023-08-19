#include <stdio.h>
int main() {
   printf("Hello, World!");
   return 0;
}

int other() {
    main();
}

int shadow() {
    int main;
    main();
}