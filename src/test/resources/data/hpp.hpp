#ifndef TEST_H
#define TEST_H

#include <iostream>

//Some definitions
extern int out;
void foo();

/*
 *  Templated function
 */
template<typename T>
void print_value(T& t)
{
    std::cout<<t;
}


#endif
