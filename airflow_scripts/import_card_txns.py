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
dag_id='One_Time_Import_Card_Txns_Data',
#schedule_interval='0 */8 * * *',
#schedule_interval='@daily',
schedule_interval=None,
start_date=days_ago(1)
)

trigger_task = BashOperator(
task_id='cc_job_trigger_task',
bash_command = 'echo Job is Starting',
dag = dag
)

def fetch_card_txns_cmd():
  command_one = f'cd Desktop'
  command_two = '{{ var.value.card_txns_import_shell_command}}'
  return f'{command_one} && {command_two}'

import_card_txns = SSHOperator(
 task_id='sqoop_import_card_txns',
 ssh_conn_id='cloudera',
 command=fetch_card_txns_cmd(),
 dag=dag
) 

dummy = DummyOperator(
 task_id='dummy',
 dag = dag 
 )

trigger_task >> import_card_txns >> dummy
