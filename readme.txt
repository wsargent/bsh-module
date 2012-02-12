This is the Bsh README.

What is Beanshell?

  Quoting from the manual: "BeanShell is a small, free, embeddable, Java source 
  interpreter with object scripting language features, written in Java. 
  BeanShell executes standard Java statements and expressions, in addition to 
  obvious scripting commands and syntax. BeanShell supports scripted objects as 
  simple method closures like those in Perl and JavaScript(tm)."

No, what is it really?

  Think of it as java code you don't have to recompile.  Like perl or something.
  You just point the interpreter at the file you want to run, and it runs it.
  
What is ATG Dynamo?

  From the manual: "The ATG Dynamo e-Business Platform is a flexible, Java-based 
  platform for building personalized applications for the Web and other 
  communication channels (e-mail messaging, wireless devices, etc.)."
  
In English?

  Dynamo does a staggering amount of stuff, all the way from managing document 
  workflow through portals to selling clothes online (and keeping track of what 
  you've bought), to booking your flight tickets.  Big enterprise level web 
  application stuff.  But basically it's a tool to make an HTML page from 
  information in a database, or change a database from an HTML page.
  
Why would I want to use Beanshell with Dynamo?

  Dynamo has great features.  Beanshell works well with them.  In particular, 
  Nucleus components works particularly well with Beanshell; since you can
  create new instances of droplets and form handlers and only deal with them
  through an interface, you don't particularly care whether they use Beanshell
  or not behind the scenes.

What is included in this package?

  A small amount of source code is included in this file showing how you can
  tweak Dynamo to execute Beanshell scripts.  There's nothing especially 
  hairy or complicated involved, except perhaps in the HttpServer.  Ant files
  are provided so that you can compile everything, and the JBuilder code project
  if you use that sort of thing.
 
  Some example beanshell scripts and components are also included.  These give
  a sample of what can be done without too much tweaking.  
  
How do I install it?

  You will need Ant and Dynamo 5 or higher to compile the code.  You should have
  an environment variable DYNAMO_HOME set to your Dynamo installation; the ant
  script will use this for finding the DAS and DPS libraries.
  
  'ant all' will compile all the necessary files and package them in an
  application module.  After that, you can run the Bsh module with 
  'startDynamo -m Bsh'.
  
What do each of the scripts do?

  converter.bsh is a fairly stupid example of a custom tag converter.  You can
  use it in conjunction with a JHTML page to alter the text that gets passed
  into a formhandler. 
   
  script.bsh is an example of repository manipulation in Beanshell.  You can
  pass in the profile repository, and it will query it for all the users and
  their properties and set output parameters and oparams on the request.
  
  server.bsh is a hack to make Serverina pass in additional headers to the 
  servlet pipeline so that we can pretend we've been passed extra info from the
  HTTP server.  
   
  rqlbrowser.zip will also show you how to make RQL queries passed into Bsh.
   
So what would I use this for?

  Here are some of the possibilities:
  
    * test code without having to create a component and write the JHTML
    * write Dynamo code within bsh without having to compile and restart Dynamo
    * script stuff and convert it to Java code
    * get a handle to the global Nucleus and run wild
    * can be set up to do some internal dynamic class loading without 
      using Dynamo's dynamic class loader
    * can be exposed through a telnet/http server interface, so that you can 
      attach to running JVMs and examine real-time state. Not very secure, but
      great for debugging. 
      
Additions and feedback is welcomed.  -- Will Sargent <will_sargent@yahoo.com>
   
Disclaimer
  
  The entire package comes with ABSOLUTELY NO WARRENTY.  You run this code at 
  your own risk.  ATG and myself are not responsible for this code and will take no
  responsibility for your use of it.  
