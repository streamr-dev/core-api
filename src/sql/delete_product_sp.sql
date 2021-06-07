drop procedure if exists delete_product;

delimiter $$
create procedure delete_product(
	in pid varchar(255)
)
begin
	declare sid varchar(255) default '';
	declare eof bool default false;
	declare cur_stream_ids cursor for
		select stream_id from product_streams where product_id = pid;
	declare continue handler for not found set eof := true;

	if pid is null or pid = '' then
		signal sqlstate '45000' set message_text = 'product id (pid) argument is required';
	end if;

	select 'starting to delete' as 'message', pid as 'product_id';
	open cur_stream_ids;
	get_stream_ids: loop
		fetch cur_stream_ids into sid;
		if eof then
			leave get_stream_ids;
		end if;
		if sid is not null then
			select sid as 'stream_id';
			delete from permission where stream_id = sid and subscription_id is not null;
			delete from product_streams where stream_id = sid;
		end if;
	end loop get_stream_ids;
	close cur_stream_ids;
	delete from subscription where product_id = pid;
	delete from permission where product_id = pid;
	delete from product where id = pid;
end$$
delimiter ;

