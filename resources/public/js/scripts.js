$( document ).ready(function() {
    console.log( "ready!" );
    $('.medias').infinitescroll({
	debug : true,

	navSelector  : "ul.navigation",            
        // selector for the paged navigation (it will be hidden)
	nextSelector : "ul.navigation a:last",    
        // selector for the NEXT link (to page 2)
	itemSelector : ".medias .media"          
        // selector for all items you'll retrieve
    });
});
