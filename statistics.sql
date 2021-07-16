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


-- select / delete vaccination slots for second shot -> useful when someone cancels the appointment
select *
from vaccination_slots
where "from" > '2021-07-16 00:31:51.463682' and
    patient_id in (select id
                   from patients
                   where personal_number in ('9112554132', '0056280026', '9762041718', '0401085773', '9309040158',
                                             '9304140021',
                                             '9851166281', '9610160670', '0551274933', '0452255573', '9353241128',
                                             '9261035410',
                                             '0107230277', '9803073357', '9604140964', '8712081158', '9253251953',
                                             '9207124091') or
                       insurance_number in ('9112554132', '0056280026', '9762041718', '0401085773', '9309040158',
                                            '9304140021',
                                            '9851166281', '9610160670', '0551274933', '0452255573', '9353241128',
                                            '9261035410',
                                            '0107230277', '9803073357', '9604140964', '8712081158', '9253251953',
                                            '9207124091'));

-- select people who did not show up for their appointment (first shot)
select first_name, last_name, personal_number, insurance_number, "from", phone_number, email
from vaccination_slots
         join patients p on p.id = vaccination_slots.patient_id
where "from" < NOW() and vaccination_id is null;

-- select people who did not show up for their appointment (second shot)
select first_name, last_name, personal_number, insurance_number, "from", phone_number, email
from vaccination_slots
         join patients p on p.id = vaccination_slots.patient_id
where "from" < NOW() and "from" > '2021-07-01 00:31:51.463682' and p.vaccination_second_dose_id is null;

-- how many people should still come
select count(*)
from vaccination_slots
where "from" >= now()
