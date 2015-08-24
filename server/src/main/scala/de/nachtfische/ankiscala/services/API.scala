package de.nachtfische.ankiscala.services

import FlashCard


trait API {
  // message of the day
  def getCards(): Seq[FlashCard]

  // get Todo items
  def getTodos(): Seq[TodoItem]

  // update a Todo
  def updateTodo(item: TodoItem): Seq[TodoItem]

  // delete a Todo
  def deleteTodo(itemId: String): Seq[TodoItem]
}


