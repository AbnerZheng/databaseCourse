/** Question 1:  Find the number of emails that mention “Obama” in the ExtractedBodyText of the email. **/
select count(1) as count  from Emails where ExtractedBodyText like '%Obama%';

/** Question 2: Among people with Aliases, find the average number of Aliases each person has. **/

SELECT
    avg(a.recount)
FROM
    (
        SELECT
        count(1) AS recount
        FROM
        Aliases
        GROUP BY
        PersonId
    ) a;

/** Question 3: Find the MetadataDateSent on which the most emails were sent and the number of emails that were sent on * that date. Note that that many emails do not have a date -- don’t include those in your count. **/

SELECT
    MetadataDateSent,
    count(1) AS rc
FROM
    Emails
GROUP BY
      (MetadataDateSent)
HAVING
    MetadataDateSent != ''
ORDER BY
      rc DESC
limit 1;

/** Question 4: Find out how many distinct ids refer to Hillary Clinton. Remember the hint from the homework spec! **/

SELECT
    count(DISTINCT Alias) as num
FROM
    Aliases,
    Persons
WHERE
    Persons.`Name` = 'Hillary Clinton' and
    Persons.`Id` = Aliases.PersonId
GROUP BY
    Aliases.PersonId

/** Question 5: Find the number of emails in the database sent by Hillary Clinton. Keep in mind that there are multiple * aliases (from the previous question) that the email could’ve been sent from. **/

SELECT
    count(1)
FROM
    Emails, Persons
where
    Persons.Name = 'Hillary Clinton' and
    Persons.Id = Emails.SenderPersonId

/** Question 6: Find the names of the 5 people who emailed Hillary Clinton the most. **/

SELECT
    Persons.Name
FROM
    Persons
INNER JOIN (
      SELECT
        Emails.SenderPersonId,
        count(1) AS rc
      FROM
        Emails
      INNER JOIN (
            SELECT DISTINCT
               Alias
            FROM
               Aliases,
               Persons
            WHERE
               Persons.Name = 'Hillary Clinton'
               AND Persons.` Id ` = Aliases.PersonId
      ) AS a ON lower(a.Alias) = lower(Emails.MetadataTo)
      GROUP BY
         Emails.SenderPersonId
      ORDER BY rc DESC
      LIMIT 5
) c
ON c.SenderPersonId = Persons.Id;

/** Question 7: Find the names of the 5 people that Hillary Clinton emailed the most. **/
SELECT Persons.Name
       from Persons
INNER JOIN
(SELECT
    PersonId, count(1) as rc
 from Aliases
 INNER JOIN
       (SELECT
           MetadataTo
        FROM
           Emails
        WHERE
           Emails.SenderPersonId = (
               SELECT
                 Id
               FROM
                 Persons
               WHERE
                 Name = 'Hillary Clinton'
 ))c
 on c.MetadataTo = Aliases.Alias
 GROUP BY PersonId
 ORDER BY rc DESC
 limit 5) cc
on cc.PersonId = Persons.Id
