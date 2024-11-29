# KotlinFBP_POC

## Example 1

<img width="403" alt="Example1" src="https://github.com/user-attachments/assets/aa56b9ef-7825-4446-bcbc-3737b4d81fe4">

A minimalist FBP example with a single output emitter node (type int->),
a single input and output processor node (int->int),


## Example 2

<img width="403" alt="Example2" src="https://github.com/user-attachments/assets/62da9654-f9db-493a-9006-7e8b02803f1b">

A minimalist FBP example with a single output emitter node (type int->),
a dual input and single output processor node (int=>int),
and a single input receiver node (->int).

## Example 3

<img width="641" alt="Example3" src="https://github.com/user-attachments/assets/c6145e26-2d0c-4353-8565-0c6a7c48b7c8">

A simple FBP example with a graph representing the Pythagorean
formula with two emitter nodes (type int->), two squared nodes (int->int)
a dual input and single output addition node (int=>int),
a single input and single output square root node (int->double)
and a single input receiver node (->double).

## Example 4

<img width="680" alt="Example4" src="https://github.com/user-attachments/assets/34635d6a-386f-40b1-89fa-00a1d8f3e04f">

A simple FBP example with a subGraph representing the Pythagorean
formula. The non-io nodes of the previous graph are encapsulated
in a subGraph function, with passThru nodes providing the ports
into and out of the subGraph.

## Example 5

<img width="842" alt="Example5" src="https://github.com/user-attachments/assets/bd9defe6-23cf-4bd0-92d5-3dec3adb708b">

A simple FBP example with a subGraph representing the Pythagorean
formula. The differences from the previous graph include a single emitter
node that produces a Pair data object (as opposed to a simple data type),
and a single input, dual output splitter node (Pair(int)=>int).

## Example 6

<img width="560" alt="Example6" src="https://github.com/user-attachments/assets/497d26bf-efee-4a53-a7a8-85b7074ae191">

A minimalist FBP example for network communication with a single output emitter node (type int->),
a ktor websocket client sending the date to a ktor websocket server,
and a single input receiver node (->int).

# Future Experiments

## Example 7

<img width="565" alt="Example7" src="https://github.com/user-attachments/assets/665f5047-5a51-4602-b638-28b726ae8697">

A minimalist FBP example for network communication with a single output emitter node (Pair(int)->),
a ktor websocket client sending the data to a ktor websocket server,
and a single input receiver node (->Pair(int)).

## Example 8

<img width="599" alt="Example8" src="https://github.com/user-attachments/assets/88bffbd0-82d5-43c1-92ec-65180dfb6857">

An FBP example for network communication with a single output emitter node (Pair(int)->),
a ktor websocket client sending the request data to a ktor websocket server,
a subGraph representing the Pythagorean formula processing on the server,
the same ktor websocket server sending the response data to the ktor websocket client,
and a single input receiver node (->int).
Question: Can a bi-directional channel handle bidirectional data flowing over a common time span?
