// add vector
__host__ void add(const int* a, const int* b, int* c) {
    int i = threadIdx.x;
    c[i] = a[i] + b[i];
}
