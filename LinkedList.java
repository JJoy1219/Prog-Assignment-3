package lockfree.linkedlist;

import java.util.concurrent.atomic.*;

class Node<T> {
	AtomicMarkableReference<Node<T>> next;
	T value;
	
	public Node(T value, Node<T> next) {
		this.next = new AtomicMarkableReference<Node<T>>(next, false);
		this.value = value;
	}
}
	
public class LinkedList<T> {
	AtomicMarkableReference<Node<T>> head;

	public static voic main (String[] args) {
		for (int i = 0;i < 4;i++)
		{
			RunnableDemo guest = new RunnableDemo(Integer.toString(i));
      		guest.start();
		}
	}

	public LinkedList() {
		Node<T> headNode = new Node<T>(null, null);
		head = new AtomicMarkableReference<Node<T>>(headNode, false);
	}
	
	public void addFirst(T value) {
		addAfter(head.getReference().value, value);
	}
	
	public boolean addAfter(T after, T value) {
		boolean sucessful = false;
		while (!sucessful) {
			boolean found = false;
			for (Node<T> node = head.getReference(); node != null && !isRemoved(node); 
			node = node.next.getReference()) {
				if (isEqual(node.value, after) && !node.next.isMarked()) {
					found = true;
					Node<T> nextNode = node.next.getReference();
					Node<T> newNode = new Node<T>(value, nextNode);
					sucessful = node.next.compareAndSet(nextNode, newNode, false, false);
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	public boolean remove(T value) {
		boolean sucessful = false;
		while (!sucessful) {
			boolean found = false;
			for (Node<T> node = head.getReference(), nextNode = node.next.getReference();
			nextNode != null; node = nextNode, nextNode = nextNode.next.getReference()) {
				if (!isRemoved(nextNode) && isEqual(nextNode.value, value)) {
					found = true;
					logicallyRemove(nextNode);
					sucessful = physicallyRemove(node, nextNode);
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	void logicallyRemove(Node<T> node) {
		while (!node.next.attemptMark(node.next.getReference(), true)) { }
	}
	
	boolean physicallyRemove(Node<T> leftNode, Node<T> node) {
		Node<T> rightNode = node;
		do {
			rightNode = rightNode.next.getReference();
		} while (rightNode != null && isRemoved(rightNode));
		return leftNode.next.compareAndSet(node, rightNode, false, false);
	}
	
	boolean isRemoved(Node<T> node) {
		return node.next.isMarked();
	}
	
	boolean isEqual(T arg0, T arg1) {
		if (arg0 == null) {
			return arg0 == arg1;
		} else {
			return arg0.equals(arg1);
		}
	}

	public void print() {
		System.out.println("result:");
		for (Node<T> node = head.getReference().next.getReference(); node != null;
		node = node.next.getReference()) {
			System.out.println(node.value);
		}

	}
}
class RunnableDemo extends LinkedList implements Runnable {
   private Thread t;
   private String threadName;
   private int guestNum;
   
   RunnableDemo( String name) {
      threadName = name;
      guestNum = Integer.parseInt(threadName);
      //System.out.println("Person " +  threadName + " is picked.");
   }
   
   public void run() {
      //System.out.println("Person " +  threadName );
      try {
         if (guestNum == counter)
         {
         	if (cakePresent == false)
         	{
         		cakePresent = true;
         		count++;
         		System.out.println("Count has been updated to " + count);
         		Thread.sleep(1);
         	}
         }
         else
         {
         	if (cakePresent == true && visited[guestNum - 1] == 0)
         	{
         		cakePresent = false;
         		visited[guestNum - 1] = 1;
         		System.out.println("Person " + threadName + " has eaten the cake.");
         	}
         }
      } catch (InterruptedException e) {
         System.out.println("Thread " +  threadName + " interrupted.");
      }
   }
   
   public void start () {
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}