select u.first_name, u.last_name, count(*)
from vaccinations
         join users u on u.id = vaccinations.user_performing_vaccination
group by u.id, u.first_name, u.last_name
order by count(*) desc;

select u.first_name, u.last_name, count(*)
from patient_data_correctness pd
         join users u on pd.user_performed_check = u.id
group by u.id, u.first_name, u.last_name, u.role
order by count(*) desc;

select q.cs as otazka, case when a.value then 'ano' else 'ne' end as odpoved, count(*) as pocet
from answers a
         join questions q on q.id = a.question_id
where value
group by q.id, q.cs, a.value
order by count(*) desc;


select count(*)
from patients;

select count(*)
from patients
where vaccination_id is not null;

select count(*)
from patients
where data_correctness_id is null;

select *
from vaccination_slots
         join patients p on p.id = vaccination_slots.patient_id
where data_correctness_id is null
order by "from";

select *
from patients
where data_correctness_id is not null and vaccination_id is null;

