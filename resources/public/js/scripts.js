/**
 * jQuery alterClass plugin
 *
 * Remove element classes with wildcard matching. Optionally add classes:
 *   $( '#foo' ).alterClass( 'foo-* bar-*', 'foobar' )
 *
 * Copyright (c) 2011 Pete Boere (the-echoplex.net)
 * Free under terms of the MIT license: http://www.opensource.org/licenses/mit-license.php
 *
 */
(function ( $ ) {
	
$.fn.alterClass = function ( removals, additions ) {
	
	var self = this;
	
	if ( removals.indexOf( '*' ) === -1 ) {
		// Use native jQuery methods if there is no wildcard matching
		self.removeClass( removals );
		return !additions ? self : self.addClass( additions );
	}
 
	var patt = new RegExp( '\\s' + 
			removals.
				replace( /\*/g, '[A-Za-z0-9-_]+' ).
				split( ' ' ).
				join( '\\s|\\s' ) + 
			'\\s', 'g' );
 
	self.each( function ( i, it ) {
		var cn = ' ' + it.className + ' ';
		while ( patt.test( cn ) ) {
			cn = cn.replace( patt, ' ' );
		}
		it.className = $.trim( cn );
	});
 
	return !additions ? self : self.addClass( additions );
};
 
})( jQuery );$( document ).ready(function() {
    console.log( "ready!" );
    $('.medias').infinitescroll({
	debug : true,

	navSelector  : "ul.navigation",            
        // selector for the paged navigation (it will be hidden)
	nextSelector : "ul.navigation a:last",    
        // selector for the NEXT link (to page 2)
	itemSelector : ".medias .media",
        // selector for all items you'll retrieve
    });


    $('.submit').on("click", (function (ev) {
    	ev.preventDefault();
    	var new_loc = "/tags/" + $("#tagname").val() + "/pages/1";
    	window.location = new_loc;
    }))

    $('.spaning .type-of-spaning').on("change", function (ev) {
	var selected = 'is-' + this.value.toLowerCase();
	$('.spaning form').alterClass( 'is-*', selected );
    });

    $('.spaning form').addClass("is-hashtag");
});
