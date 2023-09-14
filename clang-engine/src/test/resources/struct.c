struct my_list {
   int val;
   struct my_list * next;
};

typedef struct my_list l_list;

void main() {
   l_list * curr, * head;

   head = NULL;

   for (int i=0; i < 100; i++) {
      curr = (l_list *)malloc(sizeof(l_list));
      curr->val = i;
      curr->next  = head;
      head = curr;
   }
}