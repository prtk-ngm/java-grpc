package com.learning.grpc.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Worker {

  final ExecutorService executor = Executors.newFixedThreadPool(2);
  public List<Future<String>> futures = new ArrayList<Future<String>>();

  public Queue<List<Callable<String>>> q = new LinkedList<List<Callable<String>>>();

  public void start(List<Callable<String>> s){
    System.out.println("I am here");
    q.add(s);
    processTask();


  }

  public void processTask(){

    System.out.println("processTask");
    while(!q.isEmpty()){
      try {
        System.out.println("while");
        futures = executor.invokeAll(q.poll());
        for (Future<String> future : futures) {
          System.out.println(future.isDone());
          try {
            System.out.println(future.get());
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }




}
