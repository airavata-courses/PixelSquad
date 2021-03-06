# Generated by Django 2.1.5 on 2021-02-27 01:04

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('searchapi', '0002_auto_20210226_1930'),
    ]

    operations = [
        migrations.AddField(
            model_name='metadata',
            name='annotation_tags',
            field=models.CharField(default='', max_length=100),
        ),
        migrations.AddField(
            model_name='metadata',
            name='created_on',
            field=models.DateField(null=True),
        ),
        migrations.AddField(
            model_name='metadata',
            name='description',
            field=models.CharField(default='', max_length=300),
        ),
        migrations.AddField(
            model_name='metadata',
            name='photoname',
            field=models.CharField(default='', max_length=300),
        ),
    ]
