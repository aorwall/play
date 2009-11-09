package controllers

import play._
import play.mvc._
import play.data.validation._
import play.libs._
import play.cache._

import play.Scala._
import play.db.jpa._
import play.db.jpa.QueryFunctions._
 
import models._

trait Defaults extends Actions {
    
    @Before
    private def setDefaults {
        renderArgs += "blogTitle" -> Play.configuration.getProperty("blog.title")
        renderArgs += "blogBaseline" -> Play.configuration.getProperty("blog.baseline")
    }
    
}

trait Log extends Actions {
    
    @Before
    private def enter {
        println("ENTER")
    }
    
    @After
    private def exit {
        println("EXIT")
    }
    
}

object Application extends Actions with Defaults with Log {
 
    def index() { 
        val frontPost = find[Post]("order by postedAt desc").first 
        val olderPosts = find[Post]("from Post order by postedAt desc").from(1).fetch
        render(frontPost, olderPosts)
    }
    
    def show(id: Long) { 
        val post = findById[Post](id)
        val randomID = Codec.UUID
        render(post, randomID)
    }
    
    def postComment(
        postId: Long, 
        @Required(message="Author is required") author: String, 
        @Required(message="A message is required") content: String, 
        @Required(message="Please type the code") code: String, 
        randomID: String
    ) {
        val post = findById[Post](postId)
        
        Play.id match {            
            case "test" => // skip validation
            case _ => validation.equals(code, Cache get randomID) message "Invalid code. Please type it again"
        }
        
        if(Validation.hasErrors) {
            render("@show", post, randomID)
        }
        
        post.addComment(author, content)        
        flash.success("Thanks for posting %s", author)
        
        show(postId)
    }
    
    def captcha(id: String) = {
        val captcha = Images.captcha
        val code = captcha getText "#E4EAFD"
        Cache.set(id, code, "30mn")
        captcha
    }
    
    def listTagged(tag: String) {
        val posts = Post findTaggedWith tag
        render(tag, posts);
    }
 
}