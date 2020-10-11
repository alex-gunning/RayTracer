## Ray-Tracer
A ray-tracer implementation written in a loop and so can function as a graphics engine.

![Triangles](https://raw.githubusercontent.com/alex-gunning/RayTracer/master/images/traced-triangles.png)

Written from scratch in Kotlin with guidence from [https://blog.scottlogic.com/2020/03/10/raytracer-how-to.html]()

Utilises batch [APARAPI](http://aparapi.com/) OpenCL commands for GPU processing because Ray-tracing is SLOOOOW.

Code does not look pretty because it's designed to go as fast as possible.
