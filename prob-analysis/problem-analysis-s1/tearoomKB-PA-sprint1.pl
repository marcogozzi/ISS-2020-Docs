%===========================================
% tearoomkb.pl
%===========================================

%% ------------------------------------------ 
%% Positions
%% ------------------------------------------ 

pos( home, 0, 0 ).
pos( entrancedoor, 4, 1 ).
pos( exitdoor, 4, 6 ).
pos( barman, 0, 5 ).
pos( teatable(1), 2, 3 ).
pos( teatable(2), 2, 5 ).
 
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
	assert( teatable( N, reserved(Cid), clean ) ).
	
reserveTable(_,_).	

%%reservedTable(N, Cid, Clean) :- teatable( N, reserved(Cid), Clean ). %%tavolo prenotato da Cid

engageTable(N,Cid)	 :-
	retract( teatable( N, reserved(Cid), clean ) ) ,
	!,
	assert( teatable( N, busy(Cid), dirty ) ).

freeTable(N, Cid) :-
	retract( teatable( N, busy(Cid), dirty ) ),
	!,
	assert( teatable( N, free, dirty ) ).
freeTable(_,_).	
	
dirtyTable(Num) :- teatable(Num, free, dirty).	
	
cleanTable(N)	 :-
	%% stdout <- println( tearoomkb_cleanTable(N) ),
	retract( teatable( N, free, dirty ) ) ,
	!,
	asserta( teatable( N, free, clean ) ).
cleanTable(N).	
 
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
roomstate(  state( waiter(S,X,Y), tables(V) )  ):-
	 waiter(S,X,Y), stateOfTeatables(V) .
	 
	
