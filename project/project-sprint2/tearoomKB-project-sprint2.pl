%===========================================
% tearoomkb.pl
%===========================================

debug(waiter,false). 
debug(robot,false). 

%% ------------------------------------------ 
%% Clients
%% client(Cid, State).
%% State in {accepted, onhold, entering, choosing, placeorder, orderplaced, consuming, paying, leaving, left}
%% ------------------------------------------ 

updateClient(Cid,Msg) :- 
	retract(client(Cid,_)),
	!,
	assert(client(Cid,Msg)).

clients(L) :- findall(client(Cid,S),client(Cid,S),L).

%% ------------------------------------------ 
%% Configuration
%% ------------------------------------------ 

stepTime(400).
stepFailWaitTime(5000).
%%debug(x).
%%debugwaiter(x).

%% ------------------------------------------ 
%% Positions
%% ------------------------------------------ 
%%pos( home, 0, 0 ).
%%pos( entrancedoor, 4, 1 ).
%%pos( exitdoor, 4, 6 ).
%%pos( barman, 0, 5 ).
%%pos( teatable(1), 2, 3 ).
%%pos( teatable(2), 2, 5 ).
pos( home, 0, 0 ).
pos( entrancedoor, 1, 4 ).
pos( exitdoor, 5, 4 ).
pos( barman, 5, 0 ).
pos( teatable(1), 2, 2 ).
pos( teatable(2), 4, 2 ).
 
%% ------------------------------------------ 
%% Teatables
%% teatable(Num, {free, reserved(Cid), busy(Cid)}, {clean, dirty}).
%% ------------------------------------------ 
teatable( 1, free, clean ).
teatable( 2, free, clean ). 

tableavailable(N):- teatable(N,	free, clean ).

numavailabletables(N) :-
	findall( N, teatable( N, free, clean ), NList),
	length( NList, N ).

reserveTable(N,Cid)	 :-
	retract( teatable( N, free, clean ) ) ,
	!,
	assert( teatable( N, reserved(Cid), clean ) ),
	assert( client(Cid, accepted) ).
	

%%reservedTable(N, Cid, Clean) :- teatable( N, reserved(Cid), Clean ). %%tavolo prenotato da Cid

engageTable(N,Cid)	 :-
	retract( teatable( N, reserved(Cid), clean ) ) ,
	!,
	assert( teatable( N, busy(Cid), dirty ) ),
	updateClient( client(Cid, choosing) ).

freeTable(N, Cid) :-
	retract( teatable( N, busy(Cid), dirty ) ),
	!,
	assert( teatable( N, free, dirty ) ).
	
dirtyTable(Num) :- teatable(Num, free, dirty).	
	
cleanTable(N)	 :-
	%% stdout <- println( tearoomkb_cleanTable(N) ),
	retract( teatable( N, free, dirty ) ) ,
	!,
	asserta( teatable( N, free, clean ) ).
 
stateOfTeatable( [teatable1(V1,V2)] ) :-
	teatable( 1, V1, V2 ).
	
stateOfTeatables( [teatable(1, V1, V2),teatable(2, V3, V4)] ) :-
	teatable( 1, V1, V2 ),
	teatable( 2, V3, V4 ).

%% ------------------------------------------ 
%% Waiter
%% ------------------------------------------ 

waiter( rest, 0, 0 ).	

%%update textual state, keep map location
updateWaiterState(Old,New) :- 
	retract(waiter(Old, X, Y)),
	!,
	assert(waiter(New, X, Y)).

%%keep textual state, update map location (either give PositionName or coordinates of it)
updateWaiterLoc(PositionName, NewX, NewY) :- 
	pos(PositionName, NewX, NewY),
	!,
	retract(waiter(State, _, _)),
	!,
	assert(waiter(State, NewX, NewY)).

%%keep textual state, update map location to any location
updateWaiterLoc(PositionName, NewX, NewY) :- 
	retract(waiter(State, _, _)),
	!,
	assert(waiter(State, NewX, NewY)).
	
%%update textual state w/ position name, update map location to the position location
updateWaiterLoc(PositionName) :- 
	pos(PositionName, X, Y),
	!,
	retract(waiter(_, _, _)),
	!,
	assert(waiter(PositionName, X, Y)).
	
%%get both waiter and destination coordinates from destination name
coordinatesTo(PositionName, WX, WY, X, Y) :-
	waiter(_, WX, WY),
	pos(PositionName, X, Y).
%% ------------------------------------------ 
%% Hall
%% ------------------------------------------ 
hall(free).

freeHall(X) :- 
retract(hall(free)),
!,
assert(hall(busy)).

occupyHall(X) :- 
retract(hall(busy)),
!,
assert(hall(free)).


%% ------------------------------------------ 
%% Room as a whole
%% ------------------------------------------ 
roomstate(  state( waiter(S,X,Y), tables(V), clients(C) )  ):-
	 waiter(S,X,Y), stateOfTeatables(V), clients(C) .
	 
	
