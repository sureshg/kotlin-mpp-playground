package dev.suresh.lc

import dev.suresh.lc.Order.*

sealed interface Tree<out T>

data class Node<out T>(val value: T, val left: Tree<T> = Empty, val right: Tree<T> = Empty) :
    Tree<T>

data object Empty : Tree<Nothing>

enum class Order {
  PRE,
  IN,
  POST
}

fun <T> Tree<T>.dfs(order: Order = IN): List<T> =
    when (this) {
      Empty -> emptyList()
      is Node<T> -> {
        val (value, left, right) = this
        when (order) {
          IN -> left.dfs(order) + value + right.dfs(order)
          PRE -> listOf(value) + left.dfs(order) + right.dfs(order)
          POST -> left.dfs(order) + right.dfs(order) + value
        }
      }
    }

fun <T> Tree<T>.dfsTo(order: Order = IN, dest: MutableList<T>) {
  when (this) {
    Empty -> Unit
    is Node<T> -> {
      val (value, left, right) = this
      when (order) {
        PRE -> {
          dest.add(value)
          left.dfsTo(order, dest)
          right.dfsTo(order, dest)
        }

        IN -> {
          left.dfsTo(order, dest)
          dest.add(value)
          right.dfsTo(order, dest)
        }

        POST -> {
          left.dfsTo(order, dest)
          right.dfsTo(order, dest)
          dest.add(value)
        }
      }
    }
  }
}

fun <T> Tree<T>.bfs(): List<T> {
  val result = mutableListOf<T>()
  val queue = ArrayDeque<Tree<T>>()
  queue.add(this)

  while (queue.isNotEmpty()) {
    val curr = queue.removeFirst()
    when (curr) {
      Empty -> continue
      is Node<T> -> {
        result.add(curr.value)
        queue.add(curr.left)
        queue.add(curr.right)
      }
    }
  }
  return result
}

fun <T> Tree<T>.print() {
  val queue = ArrayDeque<Tree<T>>()
  queue.add(this)

  while (queue.isNotEmpty()) {
    val nodesAtLevel = queue.size
    for (i in 1..nodesAtLevel) {
      when (val curr = queue.removeFirst()) {
        Empty -> continue
        is Node<T> -> {
          print("${curr.value} ")
          queue.add(curr.left)
          queue.add(curr.right)
        }
      }
    }
    println()
  }
}

fun <T : Number> Tree<T>.sum(): Long =
    when (this) {
      Empty -> 0
      is Node<T> -> value.toLong() + left.sum() + right.sum()
    }

fun <T> Tree<T>.depth(): Int =
    when (this) {
      Empty -> 0
      is Node<T> -> 1 + maxOf(left.depth(), right.depth())
    }

fun main() {

  // Repeatedly generate parent nodes that link to the previous node as their left child
  val root1 = generateSequence(Node(1)) { Node(it.value + 1, it, Empty) }.take(100).last()

  val root =
      Node(
          1,
          left = Node(2, left = Node(4), right = Node(6)),
          right = Node(3, left = Node(5), right = Node(7)))

  println("Sum: ${root.sum()}")
  println("Depth: ${root.depth()}")

  root.print()
}
