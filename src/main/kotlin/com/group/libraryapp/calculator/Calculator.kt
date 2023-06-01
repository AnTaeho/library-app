package com.group.libraryapp.calculator

class Calculator(
    var number: Int
) {

//    val number: Int
//        get() = this._number

    fun add(operand: Int) {
        this.number += operand
    }

    fun minus(operand: Int) {
        this.number -= operand
    }

    fun multiply(operand: Int) {
        this.number *= operand
    }

    fun divide(operand: Int) {
        if (operand == 0) {
            throw IllegalArgumentException("You can't divide by 0")
        }
        this.number /= operand
    }

}