from airflow import DAG
from airflow.utils.dates import  days_ago
from airflow.utils.dates import timedelta
from airflow.operators.bash_operator import BashOperator
from airflow.sensors.http_sensor import HttpSensor
from airflow.contrib.operators.ssh_operator import SSHOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow import settings
from airflow.models import Connection

dag = DAG(
dag_id='Load_Member_Score_Data',
schedule_interval='@weekly',
start_date=days_ago(1)
)

trigger_task = BashOperator(
task_id='cc_job_trigger_task',
bash_command = 'echo Job is Starting',
dag = dag
)

def fetch_member_score_cmd():
  command_one = f'cd Desktop'
  command_two = '{{ var.value.memberscore_shell_command}}'
  return f'{command_one} && {command_two}'

import_member_score = SSHOperator(
 task_id='sqoop_member_score',
 ssh_conn_id='cloudera',
 command=fetch_member_score_cmd(),
 dag=dag
)

dummy = DummyOperator(
 task_id='dummy',
 dag = dag 
 )

trigger_task >> import_member_score >> dummy
